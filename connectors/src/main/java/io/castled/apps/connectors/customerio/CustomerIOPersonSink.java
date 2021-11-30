package io.castled.apps.connectors.customerio;

import com.google.common.collect.Maps;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.customerio.client.CustomerIORestClient;
import io.castled.apps.models.DataSinkRequest;
import io.castled.apps.models.GenericSyncObject;
import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.errorclassifications.UnclassifiedError;
import io.castled.commons.models.AppSyncStats;
import io.castled.commons.models.MessageSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.core.CastledOffsetQueue;
import io.castled.schema.SchemaUtils;
import io.castled.schema.models.Field;
import io.castled.schema.models.Message;
import io.castled.schema.models.Tuple;
import io.castled.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;


@Slf4j
public class CustomerIOPersonSink implements  CustomerIOObjectSink<String> {

    private final ErrorOutputStream errorOutputStream;
    private final CustomerIOAppSyncConfig syncConfig;
    private final AppSyncStats syncStats;
    private final List<String> primaryKeys;
    private final List<String> mappedFields;

    private final CustomerIORestClient customerIORestClient;
    private final CustomerIOErrorParser customerIOErrorParser;
    private final GenericSyncObject audienceSyncObject;

    private final AtomicLong failedRecords = new AtomicLong(0);
    private final AtomicLong processedRecords = new AtomicLong(0);

    private long lastProcessedOffset = 0;

    private final CastledOffsetQueue<Message> companyRecordsBuffer =
            new CastledOffsetQueue<>(new CustomerIOPersonSink.PersonConsumer(), 10, 10, true);


    private class PersonConsumer implements Consumer<Message> {

        @Override
        public void accept(Message message) {
            Map<String, Object> companyProperties = constructProperties(message.getRecord());
            try {
                customerIORestClient.upsertPersonDetails(companyProperties,primaryKeys);
            } catch (Exception e) {
                failedRecords.incrementAndGet();
                CastledError pipelineError = customerIOErrorParser.getPipelineError(e.getLocalizedMessage());
                errorOutputStream.writeFailedRecord(message, pipelineError);
            }
            processedRecords.incrementAndGet();
        }
    }

    public CustomerIOPersonSink(DataSinkRequest dataSinkRequest) {
        this.customerIORestClient = new CustomerIORestClient(((CustomerIOAppConfig) dataSinkRequest.getExternalApp().getConfig()).getSiteId(),
                ((CustomerIOAppConfig) dataSinkRequest.getExternalApp().getConfig()).getApiKey());
        this.errorOutputStream = dataSinkRequest.getErrorOutputStream();
        this.customerIOErrorParser = ObjectRegistry.getInstance(CustomerIOErrorParser.class);
        this.audienceSyncObject = (GenericSyncObject) dataSinkRequest.getAppSyncConfig().getObject();
        this.syncConfig = (CustomerIOAppSyncConfig) dataSinkRequest.getAppSyncConfig();
        this.primaryKeys = dataSinkRequest.getPrimaryKeys();
        this.syncStats = new AppSyncStats(0, 0, 0);
        this.mappedFields = dataSinkRequest.getMappedFields();
    }



    private String getEmail(Tuple record) {
        return (String) record.getValue(CustomerIOObjectFields.CONTACTS_FIELDS.EMAIL.getFieldName());
    }

    private String getId(Tuple record) {
        return (String) record.getValue(CustomerIOObjectFields.CONTACTS_FIELDS.ID.getFieldName());
    }

    public void createOrUpdateObject(Message message) {
        try {
            companyRecordsBuffer.writePayload(message, 5, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            //log.error("Unable to publish record to records queue", e);
            errorOutputStream.writeFailedRecord(message,
                    new UnclassifiedError("Internal error! Unable to publish records to records queue. Please contact support"));
        }
    }

    @Override
    public void flushRecords() throws TimeoutException {
        companyRecordsBuffer.flush(TimeUtils.minutesToMillis(10));
    }

    @Override
    public MessageSyncStats getSyncStats() {
        return new MessageSyncStats(processedRecords.get(), companyRecordsBuffer.getProcessedOffset());
    }

    private Map<String, Object> constructProperties(Tuple record) {
        Map<String, Object> recordProperties = Maps.newHashMap();
        for (Field field : record.getFields()) {
            Object value = record.getValue(field.getName());
            if (value != null) {
                if (SchemaUtils.isZonedTimestamp(field.getSchema())) {
                    recordProperties.put(field.getName(), ((ZonedDateTime) value).toEpochSecond());
                } if (value instanceof LocalDate) {
                    recordProperties.put(field.getName(), ((LocalDate) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                }
                else if (value instanceof LocalDateTime) {
                    recordProperties.put(field.getName(), ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                }
                else {
                    recordProperties.put(field.getName(), value);
                }
            }
        }
        return recordProperties;
    }
}
