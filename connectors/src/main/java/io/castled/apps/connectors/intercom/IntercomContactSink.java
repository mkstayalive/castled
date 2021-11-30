package io.castled.apps.connectors.intercom;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.castled.apps.models.PrimaryKeyIdMapper;
import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.apps.connectors.intercom.client.IntercomObjectFields;
import io.castled.apps.connectors.intercom.client.IntercomRestClient;
import io.castled.apps.connectors.intercom.client.dtos.DataAttribute;
import io.castled.apps.connectors.intercom.client.exceptions.IntercomRestException;
import io.castled.apps.connectors.intercom.client.models.IntercomModel;
import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.errorclassifications.UnclassifiedError;
import io.castled.commons.models.MessageSyncStats;
import io.castled.commons.models.ObjectIdAndMessage;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.core.CastledOffsetQueue;
import io.castled.schema.SchemaUtils;
import io.castled.schema.models.Field;
import io.castled.schema.models.Message;
import io.castled.schema.models.Tuple;
import io.castled.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Slf4j
public class IntercomContactSink implements IntercomObjectSink<String> {

    private IntercomRestClient intercomRestClient;
    private List<String> customAttributes;
    private PrimaryKeyIdMapper<String> primaryKeyIdMapper;
    private IntercomObject intercomObject;
    private List<String> primaryKeys;
    private final AtomicLong processedRecords = new AtomicLong(0);

    private ErrorOutputStream errorOutputStream;
    private final IntercomErrorParser intercomErrorParser = new IntercomErrorParser();
    private final CastledOffsetQueue<ObjectIdAndMessage> recordsBuffer =
            new CastledOffsetQueue<>(new ContactConsumer(), 20, 60, true);

    private class ContactConsumer implements Consumer<ObjectIdAndMessage> {

        @Override
        public void accept(ObjectIdAndMessage objectIdAndMessage) {
            if (objectIdAndMessage.getId() == null) {
                createContact(objectIdAndMessage.getMessage());
                return;
            }
            updateContact(objectIdAndMessage.getMessage(), objectIdAndMessage.getId());
        }

        private void createContact(Message message) {
            Map<String, Object> contactProperties = constructContactProperties(message.getRecord());
            if (Lists.newArrayList(IntercomObject.LEAD, IntercomObject.USER).contains(intercomObject)) {
                contactProperties.put(IntercomObjectFields.ROLE, intercomObject.getName().toLowerCase());
            }
            try {
                intercomRestClient.createContact(contactProperties, customAttributes);
            } catch (IntercomRestException e) {
                CastledError pipelineError = intercomErrorParser.parseIntercomError(e.getErrorResponse());
                errorOutputStream.writeFailedRecord(message, pipelineError);
            }
            processedRecords.incrementAndGet();
        }

        private void updateContact(Message message, String id) {
            Map<String, Object> contactProperties = constructContactProperties(message.getRecord());
            if (Lists.newArrayList(IntercomObject.LEAD, IntercomObject.USER).contains(intercomObject)) {
                contactProperties.put(IntercomObjectFields.ROLE, intercomObject.getName().toLowerCase());
            }
            try {
                intercomRestClient.updateContact(id, contactProperties, customAttributes);
            } catch (IntercomRestException e) {
                CastledError pipelineError = intercomErrorParser.parseIntercomError(e.getErrorResponse());
                errorOutputStream.writeFailedRecord(message, pipelineError);
            }
            processedRecords.incrementAndGet();
        }
    }

    @Override
    public IntercomObjectSink<String> initialize(IntercomObject intercomObject, AppSyncConfig appSyncConfig,
                                                 IntercomAppConfig intercomAppConfig, ErrorOutputStream errorOutputStream,
                                                 List<String> primaryKeys) {
        this.intercomRestClient = new IntercomRestClient(intercomAppConfig.getAccessToken());
        this.intercomObject = intercomObject;
        this.primaryKeys = primaryKeys;


        this.customAttributes = intercomRestClient.listAttributes(IntercomModel.CONTACT)
                .stream().filter(DataAttribute::isCustom).map(DataAttribute::getName).collect(Collectors.toList());

        this.primaryKeyIdMapper = constructPrimaryKeyIdMapper(appSyncConfig);
        this.errorOutputStream = errorOutputStream;

        return this;
    }

    @Override
    public PrimaryKeyIdMapper<String> getPrimaryKeyIdMapper() {
        return primaryKeyIdMapper;
    }

    @Override
    public void createObject(Message message) {
        try {
            recordsBuffer.writePayload(new ObjectIdAndMessage(null, message), 5, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            log.error("Unable to publish record to records queue", e);
            errorOutputStream.writeFailedRecord(message,
                    new UnclassifiedError("Internal error!! Unable to publish records to records queue. Please contact support"));
        }

    }

    @Override
    public void updateObject(String id, Message message) {
        try {
            recordsBuffer.writePayload(new ObjectIdAndMessage(id, message), 5, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            log.error("Unable to publish record to records queue", e);
            errorOutputStream.writeFailedRecord(message,
                    new UnclassifiedError("Internal error!! Unable to publish records to records queue. Please contact support"));
        }
    }

    @Override
    public void flushRecords() throws TimeoutException {
        recordsBuffer.flush(TimeUtils.minutesToMillis(10));
    }

    @Override
    public MessageSyncStats getSyncStats() {
        return new MessageSyncStats(processedRecords.get(), recordsBuffer.getProcessedOffset());
    }


    private Map<String, Object> constructContactProperties(Tuple record) {
        Map<String, Object> recordProperties = Maps.newHashMap();
        for (Field field : record.getFields()) {
            Object value = record.getValue(field.getName());
            if (value != null) {
                if (SchemaUtils.isZonedTimestamp(field.getSchema())) {
                    recordProperties.put(field.getName(), ((ZonedDateTime) value).toEpochSecond());
                } else {
                    recordProperties.put(field.getName(), value);
                }
            }
        }
        return recordProperties;
    }

    private PrimaryKeyIdMapper<String> constructPrimaryKeyIdMapper(AppSyncConfig appSyncConfig) {
        Map<List<Object>, String> primaryKeyToIdMapping = Maps.newHashMap();
        this.intercomRestClient.consumeContacts(properties -> {
            String id = (String) properties.get(IntercomObjectFields.INTERCOM_ID);
            primaryKeyToIdMapping.put(primaryKeys
                    .stream().map(properties::get).collect(Collectors.toList()), id);
        });

        return new PrimaryKeyIdMapper<>(primaryKeyToIdMapping);
    }
}
