package io.castled.apps.connectors.salesforce;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.castled.apps.DataSink;
import io.castled.apps.OAuthAppConfig;
import io.castled.apps.connectors.salesforce.client.SFDCBulkClient;
import io.castled.apps.connectors.salesforce.client.SFDCRestClient;
import io.castled.apps.connectors.salesforce.client.dtos.*;
import io.castled.apps.models.DataSinkRequest;
import io.castled.apps.models.GenericSyncObject;
import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.commons.models.AppSyncMode;
import io.castled.commons.models.AppSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.exceptions.CastledException;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.SchemaUtils;
import io.castled.schema.models.*;
import io.castled.utils.SizeUtils;
import io.castled.utils.ThreadUtils;
import io.castled.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.*;
import org.apache.commons.io.input.CharSequenceReader;

import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
public class SalesforceDataSink implements DataSink {

    private CSVPrinter csvPrinter;
    private List<String> trackableFields;
    private final List<Object> existingPrimaryKeyValues = Lists.newArrayList();

    private final AtomicLong skippedRecords = new AtomicLong(0);
    private final AtomicLong processedRecords = new AtomicLong(0);

    private final SalesforceSinkConfig salesforceSinkConfig;
    private final SalesforceFailedRecordsSchemaMapper salesforceFailedRecordsSchemaMapper;
    private final SalesforceErrorParser salesforceErrorParser;


    @Inject
    public SalesforceDataSink(SalesforceSinkConfig salesforceSinkConfig, SalesforceFailedRecordsSchemaMapper salesforceFailedRecordsSchemaMapper,
                              SalesforceErrorParser salesforceErrorParser) {
        this.salesforceSinkConfig = salesforceSinkConfig;
        this.salesforceFailedRecordsSchemaMapper = salesforceFailedRecordsSchemaMapper;
        this.salesforceErrorParser = salesforceErrorParser;
    }

    @Override
    public void syncRecords(DataSinkRequest dataSinkRequest) throws Exception {

        StringBuilder recordsBuffer = null;
        OAuthAppConfig salesforceAppConfig = (OAuthAppConfig) dataSinkRequest.getExternalApp().getConfig();
        SFDCRestClient sfdcRestClient = new SFDCRestClient(salesforceAppConfig.getOAuthToken(),
                salesforceAppConfig.getClientConfig());
        computeExistingPrimaryKeysIfReqd(salesforceAppConfig, dataSinkRequest.getPrimaryKeys(), dataSinkRequest.getAppSyncConfig());

        Message message;
        long recordsBuffered = 0;
        String primaryKey = dataSinkRequest.getPrimaryKeys().get(0);
        Map<Object, Long> primaryKeyOffsetMapper = Maps.newHashMap();

        while ((message = dataSinkRequest.getMessageInputStream().readMessage()) != null) {
            if (this.csvPrinter == null) {
                recordsBuffer = new StringBuilder();
                this.trackableFields = message.getRecord().getFields().stream().map(Field::getName)
                        .filter(dataSinkRequest.getMappedFields()::contains).collect(Collectors.toList());

                this.csvPrinter = new CSVPrinter(recordsBuffer, CSVFormat.DEFAULT
                        .withHeader(trackableFields.toArray(new String[0])).withQuoteMode(QuoteMode.ALL));
            }

            if (this.appendRecordToBuffer(message, dataSinkRequest.getAppSyncConfig(),
                    dataSinkRequest.getPrimaryKeys())) {
                primaryKeyOffsetMapper.putIfAbsent(message.getRecord().getValue(primaryKey), message.getOffset());
                recordsBuffered++;
            } else {
                skippedRecords.incrementAndGet();
            }
            if (recordsBuffered > 0 && (recordsBuffer.length() > SizeUtils.convertMBToBytes(salesforceSinkConfig.getRequestBufferThreshold()))) {
                this.csvPrinter.flush();
                this.csvPrinter.close();
                uploadBufferedRecords(recordsBuffered, sfdcRestClient, dataSinkRequest.getAppSyncConfig(), recordsBuffer,
                        dataSinkRequest.getObjectSchema(), dataSinkRequest.getErrorOutputStream(),
                        primaryKey, primaryKeyOffsetMapper);
                primaryKeyOffsetMapper.clear();
                this.csvPrinter = null;

                recordsBuffered = 0;
                recordsBuffer = null;
            }
        }
        if (this.csvPrinter != null) {
            this.csvPrinter.flush();
            this.csvPrinter.close();
        }
        if (recordsBuffered > 0) {
            uploadBufferedRecords(recordsBuffered, sfdcRestClient, dataSinkRequest.getAppSyncConfig(), recordsBuffer,
                    dataSinkRequest.getObjectSchema(), dataSinkRequest.getErrorOutputStream(),
                    primaryKey, primaryKeyOffsetMapper);
        }
    }

    @Override
    public AppSyncStats getSyncStats() {
        return new AppSyncStats(processedRecords.get(), 0, skippedRecords.get());
    }

    private void computeExistingPrimaryKeysIfReqd(OAuthAppConfig salesforceAppConfig, List<String> primaryKeys,
                                                  AppSyncConfig appSyncConfig) throws Exception {

        GenericSyncObject sfdcSyncObject = (GenericSyncObject) appSyncConfig.getObject();
        if (appSyncConfig.getMode() == AppSyncMode.UPDATE) {
            String primaryKey = primaryKeys.get(0);
            SFDCBulkClient sfdcBulkClient = new SFDCBulkClient(salesforceAppConfig.getOAuthToken(),
                    salesforceAppConfig.getClientConfig());

            String query = String.format("select %s from %s", primaryKey, sfdcSyncObject.getObjectName());
            sfdcBulkClient.runBulkQuery(query, PkChunking.builder().chunkSize(50000).enabled(true).build(), sfdcSyncObject.getObjectName(),
                    TimeUtils.minutesToMillis(10), record -> existingPrimaryKeyValues.add(record.get(primaryKey)));
        }
    }

    private void uploadBufferedRecords(long recordsBuffered, SFDCRestClient sfdcRestClient, AppSyncConfig appSyncConfig, StringBuilder recordsBuffer,
                                       RecordSchema objectSchema, ErrorOutputStream errorOutputStream,
                                       String primaryKey, Map<Object, Long> offsetMapper) throws Exception {

        Job job = sfdcRestClient.createJob(createJobRequest(appSyncConfig, primaryKey));
        sfdcRestClient.uploadCsv(job.getId(), recordsBuffer.toString());
        sfdcRestClient.updateJobState(job.getId(), new JobStateUpdateRequest(JobState.UPLOAD_COMPLETE));
        long startTime = System.currentTimeMillis();
        ThreadUtils.interruptIgnoredSleep(TimeUtils.secondsToMillis(10));
        int iterations = 0;
        while (true) {
            job = sfdcRestClient.getJob(job.getId());
            if (Lists.newArrayList(JobState.JOB_COMPLETE, JobState.ABORTED, JobState.FAILED).contains(job.getState())) {
                break;
            }
            if (System.currentTimeMillis() - startTime > TimeUtils.minutesToMillis(salesforceSinkConfig.getUploadTimeoutMins())) {
                throw new TimeoutException();
            }
            iterations++;
            ThreadUtils.interruptIgnoredSleep(iterations * TimeUtils.secondsToMillis(5));
        }

        processFailedReport(new CharSequenceReader(sfdcRestClient.getFailedReport(job.getId())), objectSchema,
                errorOutputStream, primaryKey, offsetMapper);
        processedRecords.addAndGet(recordsBuffered);

    }

    private void processFailedReport(Reader reportReader, RecordSchema objectSchema,
                                     ErrorOutputStream errorOutputStream, String primaryKey,
                                     Map<Object, Long> offsetMapper) throws CastledException {
        try {
            CSVParser csvParser = new CSVParser(reportReader, CSVFormat.RFC4180.withHeader().withSkipHeaderRecord());
            for (CSVRecord csvRecord : csvParser) {
                String sfError = csvRecord.get("sf__Error");
                Tuple.Builder recordBuilder = Tuple.builder();
                for (FieldSchema fieldSchema : objectSchema.getFieldSchemas()) {
                    if (!primaryKey.equals(fieldSchema.getName()) || !csvRecord.isSet(fieldSchema.getName())) {
                        continue;
                    }
                    String valueStr = csvRecord.get(fieldSchema.getName());
                    if (valueStr != null) {
                        recordBuilder.put(fieldSchema, salesforceFailedRecordsSchemaMapper.transformValue(valueStr, fieldSchema.getSchema()));
                    }
                }
                Tuple record = recordBuilder.build();
                errorOutputStream.writeFailedRecord(new Message(Optional.ofNullable(offsetMapper.get(record.getValue(primaryKey)))
                        .orElse(0L), record), this.salesforceErrorParser.parseSalesforceError(sfError));
            }
        } catch (Exception e) {
            log.error("Process failed records failed", e);
            throw new CastledException(e);
        }
    }


    private JobRequest createJobRequest(AppSyncConfig appSyncConfig, String primaryKey) {

        GenericSyncObject sfdcSyncObject = (GenericSyncObject) appSyncConfig.getObject();
        switch (appSyncConfig.getMode()) {
            case UPSERT:
                return new UpsertJobRequest(sfdcSyncObject.getObjectName(), ContentType.CSV, primaryKey);
            case INSERT:
                return new InsertJobRequest(sfdcSyncObject.getObjectName(), ContentType.CSV);
            case UPDATE:
                return new UpsertJobRequest(sfdcSyncObject.getObjectName(), ContentType.CSV, primaryKey);
            default:
                throw new CastledRuntimeException("Unhandled app sync mode: " + appSyncConfig.getMode());
        }
    }


    private boolean appendRecordToBuffer(Message message, AppSyncConfig appSyncConfig, List<String> primaryKeys)
            throws IOException {
        if (appSyncConfig.getMode() == AppSyncMode.UPDATE) {
            String primaryKey = primaryKeys.get(0);
            if (!existingPrimaryKeyValues.contains(message.getRecord().getValue(primaryKey))) {
                return false;
            }
        }
        List<String> transformedValues = Lists.newArrayList();
        for (Field field : message.getRecord().getFields()) {
            if (trackableFields.contains(field.getName())) {
                String transformedValue = Optional.ofNullable(transformValue(message.getRecord().getValue(field.getName()), field.getSchema()))
                        .orElse("#N/A");
                transformedValues.add(transformedValue);
            }
        }
        this.csvPrinter.printRecord(transformedValues);
        return true;
    }

    private String transformValue(Object value, Schema schema) {
        if (value == null) {
            return null;
        }
        if (SchemaUtils.isZonedTimestamp(schema)) {
            ZonedDateTime zonedDateTime = (ZonedDateTime) value;
            return zonedDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
        }
        if (SchemaUtils.isDateSchema(schema)) {
            LocalDate localDate = (LocalDate) value;
            return localDate.format(DateTimeFormatter.ISO_DATE);
        }
        if (SchemaUtils.isTimeSchema(schema)) {
            LocalTime localTime = (LocalTime) value;
            return localTime.format(DateTimeFormatter.ISO_TIME);
        }
        return value.toString();
    }

}