package io.castled.apps.connectors.marketo;

import com.google.api.client.util.Lists;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import io.castled.apps.BufferedObjectSink;
import io.castled.apps.connectors.marketo.dtos.GenericObjectSyncRequest;
import io.castled.apps.models.DataSinkRequest;
import io.castled.commons.errors.errorclassifications.ExternallyCategorizedError;
import io.castled.commons.models.AppSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.models.Message;

import java.util.List;
import java.util.Map;

public class MarketoGenericObjectSink extends BufferedObjectSink<Message> {

    // Marketo API Limits
    // Max number of records in a batch rest api request
    private static final long BATCH_REQUEST_NUM_MAX = 300;

    private final MarketoBulkClient marketoClient;
    private final ErrorOutputStream errorOutputStream;
    private final MarketoAppSyncConfig syncConfig;
    private final List<String> primaryKeys;
    private final AppSyncStats syncStats;

    MarketoGenericObjectSink(DataSinkRequest dataSinkRequest) {
        this.marketoClient = new MarketoBulkClient((MarketoAppConfig) dataSinkRequest.getExternalApp().getConfig());
        this.errorOutputStream = dataSinkRequest.getErrorOutputStream();
        this.syncConfig = (MarketoAppSyncConfig) dataSinkRequest.getAppSyncConfig();
        this.primaryKeys = dataSinkRequest.getPrimaryKeys();
        this.syncStats = new AppSyncStats(0, 0, 0);
    }

    @Override
    protected void writeRecords(List<Message> records) {
        GenericObjectSyncRequest request = this.constructSyncRequest(records);
        MarketoObject marketoObject = MarketoObject.getObjectByName(this.syncConfig.getObject().getObjectName());
        BatchSyncStats batchSyncStats = this.marketoClient.batchSyncObject(marketoObject, request);
        batchSyncStats.getErrors()
                .forEach(errorRec -> this.errorOutputStream.writeFailedRecord(records.get(errorRec.getMsgIdx()),
                        new ExternallyCategorizedError(errorRec.getErrorCode(), errorRec.getMessage())));
        updateSyncStats(records.size(), Iterables.getLast(records).getOffset(), batchSyncStats.getSkipped());
    }

    @Override
    public long getMaxBufferedObjects() {
        return BATCH_REQUEST_NUM_MAX;
    }

    public AppSyncStats getSyncStats() {
        return this.syncStats;
    }

    GenericObjectSyncRequest constructSyncRequest(List<Message> records) {
        GenericObjectSyncRequest request = new GenericObjectSyncRequest();
        request.setAction(getMarketoSyncMode());
        request.setDedupeBy(getDedupeKey(records));
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
        // Only 1 pk allowed
        String pkDisplayName = this.primaryKeys.stream().findFirst().orElse(null);
        Message msg = records.stream().findFirst().orElseThrow(() -> new CastledRuntimeException("Records empty!"));
        // Marketo apis are weird!
        return (String) msg.getRecord().getField(pkDisplayName).getParams().get("fieldName");
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

    private void updateSyncStats(long processed, long offset, long recordsSkipped) {
        syncStats.setRecordsProcessed(processed + syncStats.getRecordsProcessed());
        syncStats.setOffset(Math.max(offset, syncStats.getOffset()));
        syncStats.setRecordsSkipped(recordsSkipped + syncStats.getRecordsSkipped());
    }
}