package io.castled.apps.connectors.marketo;

import com.google.api.client.util.Lists;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import io.castled.apps.BufferedObjectSink;
import io.castled.apps.connectors.marketo.dtos.BatchLeadUpdateRequest;
import io.castled.apps.models.DataSinkRequest;
import io.castled.commons.errors.errorclassifications.ExternallyCategorizedError;
import io.castled.commons.models.AppSyncMode;
import io.castled.commons.models.AppSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.functionalinterfaces.ThrowingConsumer;
import io.castled.schema.models.Message;
import io.castled.schema.models.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class MarketoLeadSink extends BufferedObjectSink<Message> {

    // Marketo API Limits
    // Maximum bulk request file size
    private static final long BULK_REQUEST_BYTES_SIZE_MAX = 10 * FileUtils.ONE_MB;
    // This number chosen randomly, based on the assumption that 30K records won't consume more than 10Mb file size.
    // This is a hack as BufferedObjectSink currently needs a batch size in terms of number of records.
    private static final long BULK_REQUEST_NUM_MAX = 30000;
    // Maximum number of records in batch request
    private static final long BATCH_REQUEST_NUM_MAX = 300;


    private final MarketoBulkClient marketoBulkClient;
    private final ErrorOutputStream errorOutputStream;
    private final MarketoAppSyncConfig syncConfig;
    private final AppSyncStats syncStats;
    private final String pkDisplayName;
    private final List<String> mappedFields;

    public MarketoLeadSink(DataSinkRequest dataSinkRequest) {
        this.marketoBulkClient = new MarketoBulkClient((MarketoAppConfig) dataSinkRequest.getExternalApp().getConfig());
        this.errorOutputStream = dataSinkRequest.getErrorOutputStream();
        this.syncConfig = (MarketoAppSyncConfig) dataSinkRequest.getAppSyncConfig();
        this.syncStats = new AppSyncStats(0,0,0);
        if (CollectionUtils.size(dataSinkRequest.getPrimaryKeys()) > 1) {
            log.error("Only 1 primary key allowed, we have more => " + dataSinkRequest.getPrimaryKeys());
        }
        this.pkDisplayName = dataSinkRequest.getPrimaryKeys().stream().findFirst().get();
        this.mappedFields = dataSinkRequest.getMappedFields();
    }

    @Override
    protected void writeRecords(List<Message> msgs) {
        List<MarketoSyncError> upsertErrors;
        long skipped = 0;
        if (syncConfig.getMode() == AppSyncMode.UPSERT) {
            // Bulk apis available only for upserts
            ByteArrayOutputStream leadsStream = constructLeadFormData(msgs);
            String primaryKey = getPrimaryKeyName(pkDisplayName, msgs.stream().findFirst().get().getRecord());
            upsertErrors = marketoBulkClient.bulkUploadLeads(leadsStream, primaryKey, msgs.size());
        } else if (syncConfig.getMode() == AppSyncMode.UPDATE) {
            BatchLeadUpdateRequest request = constructLeadUpdateRequest(msgs);
            BatchSyncStats syncStats = marketoBulkClient.batchUpdateLeads(request);
            upsertErrors = syncStats.getErrors();
            skipped = syncStats.getSkipped();
        } else {
            throw new CastledRuntimeException(String.format("Invalid sync mode %s", syncConfig.getMode()));
        }

        upsertErrors.forEach(errorRec -> errorOutputStream.writeFailedRecord(msgs.get(errorRec.getMsgIdx()),
                new ExternallyCategorizedError(errorRec.getErrorCode(), errorRec.getMessage())));
        updateStats(msgs.size(), Iterables.getLast(msgs).getOffset(), skipped);
    }

    @Override
    public long getMaxBufferedObjects() {
        switch (syncConfig.getMode()) {
            case UPDATE:
                return BATCH_REQUEST_NUM_MAX;
            case UPSERT:
                return BULK_REQUEST_NUM_MAX;
            default:
                throw new CastledRuntimeException(String.format("Invalid sync mode %s!", syncConfig.getMode().name()));
        }
    }

    private ByteArrayOutputStream constructLeadFormData(List<Message> msgs) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedWriter baosWriter = new BufferedWriter(new OutputStreamWriter(baos));
        // Use first tuple to get all the possible fields for this sync.
        // Assumption is that all the tuple have the same set of schema.
        Tuple rec = msgs.stream().findFirst().get().getRecord();
        // CSV headers should have rest header names for the fields.
        // TODO Verify
        List<String> restHeader = this.mappedFields.stream().map(fieldName -> (String)rec.getField(fieldName).getParams().get("name"))
                .collect(Collectors.toList());
        List<String> header = rec.getFields().stream().map(fieldRef -> fieldRef.getName())
                .collect(Collectors.toList());

        try {
            CSVPrinter csvPrinter = new CSVPrinter(baosWriter,
                    CSVFormat.DEFAULT.withHeader(restHeader.stream().toArray(String[]::new)));
            ThrowingConsumer<Tuple> writeTuple = (tuple) -> {
                csvPrinter.printRecord(header.stream()
                        .map(fieldName -> MarketoUtils.formatValue(tuple.getValue(fieldName), tuple.getField(fieldName).getSchema()))
                        .collect(Collectors.toList()).toArray());
            };
            // Write rows to CSV byte stream
            for (Message msgRef : msgs) {
                writeTuple.accept(msgRef.getRecord());
            }
            csvPrinter.close();
        } catch (Exception e) {
            throw new CastledRuntimeException(e);
        }
        return baos;
    }

    BatchLeadUpdateRequest constructLeadUpdateRequest(List<Message> records) {
        BatchLeadUpdateRequest request = new BatchLeadUpdateRequest();
        request.setAction(getMarketoSyncMode());
        request.setLookupField(getDedupeKey(records));
        List<Map<String, Object>> input = Lists.newArrayList();

        for (Message msg : records) {
            Map<String, Object> inputRec = Maps.newHashMap();
            msg.getRecord().getFields()
                    .forEach(fieldRef -> inputRec.put((String) fieldRef.getParams().get("name"),
                            MarketoUtils.formatValue(fieldRef.getValue(), fieldRef.getSchema())));
            input.add(inputRec);
        }
        request.setInput(input);
        return request;
    }

    // Get api name from display name
    private String getDedupeKey(List<Message> records) {
        Message msg = records.stream().findFirst().orElseThrow(() -> new CastledRuntimeException("Records empty!"));
        return (String) msg.getRecord().getField(pkDisplayName).getParams().get("name");
    }

    private String getMarketoSyncMode() {
        switch (this.syncConfig.getMode()) {
            case UPDATE:
                return MarketoSyncMode.UPDATE.getName();
            case INSERT:
                return MarketoSyncMode.INSERT.getName();
            case UPSERT:
                return MarketoSyncMode.UPSERT.getName();
            default:
                throw new CastledRuntimeException(String.format("Invalid sync mode %s!", syncConfig.getMode().name()));
        }
    }

    // Get the api name of the primary key from it's display name
    private String getPrimaryKeyName(String displayName, Tuple tuple) {
        return this.mappedFields.stream().filter(fieldName -> displayName.equals(fieldName))
                .map(fieldName -> (String)tuple.getField(fieldName).getParams().get("name")).findFirst().get();
    }

    private void updateStats(long processed, long maxOffset, long skipped) {
        this.syncStats.setRecordsProcessed(this.syncStats.getRecordsProcessed() + processed);
        this.syncStats.setOffset(Math.max(this.syncStats.getOffset(), maxOffset));
        this.syncStats.setRecordsSkipped(this.syncStats.getRecordsSkipped() + skipped);
    }

    public AppSyncStats getSyncStats() {
        return this.syncStats;
    }
}

