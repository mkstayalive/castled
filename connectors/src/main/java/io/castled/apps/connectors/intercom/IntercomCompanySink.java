package io.castled.apps.connectors.intercom;

import com.google.common.collect.Maps;
import io.castled.apps.models.PrimaryKeyIdMapper;
import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.apps.connectors.intercom.client.IntercomRestClient;
import io.castled.apps.connectors.intercom.client.dtos.DataAttribute;
import io.castled.apps.connectors.intercom.client.exceptions.IntercomRestException;
import io.castled.apps.connectors.intercom.client.models.IntercomModel;
import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.errorclassifications.UnclassifiedError;
import io.castled.commons.models.MessageSyncStats;
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
public class IntercomCompanySink implements IntercomObjectSink<String> {

    private IntercomRestClient intercomRestClient;
    private List<String> customAttributes;
    private PrimaryKeyIdMapper<String> primaryKeyIdMapper;
    private ErrorOutputStream errorOutputStream;
    private final IntercomErrorParser errorParser = new IntercomErrorParser();

    private final AtomicLong failedRecords = new AtomicLong(0);
    private final AtomicLong processedRecords = new AtomicLong(0);
    private final CastledOffsetQueue<Message> companyRecordsBuffer =
            new CastledOffsetQueue<>(new CompanyRecordConsumer(), 2, 10, true);


    private class CompanyRecordConsumer implements Consumer<Message> {

        @Override
        public void accept(Message message) {
            Map<String, Object> companyProperties = constructProperties(message.getRecord());
            try {
                intercomRestClient.createCompany(companyProperties, customAttributes);
            } catch (IntercomRestException e) {
                failedRecords.incrementAndGet();
                CastledError pipelineError = errorParser.parseIntercomError(e.getErrorResponse());
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

        this.customAttributes = intercomRestClient.listAttributes(IntercomModel.COMPANY)
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
            companyRecordsBuffer.writePayload(message, 5, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            log.error("Unable to publish record to records queue", e);
            errorOutputStream.writeFailedRecord(message,
                    new UnclassifiedError("Internal error! Unable to publish records to records queue. Please contact support"));
        }
    }


    @Override
    public void updateObject(String id, Message message) {

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
                } else {
                    recordProperties.put(field.getName(), value);
                }
            }
        }
        return recordProperties;
    }

    private PrimaryKeyIdMapper<String> constructPrimaryKeyIdMapper(AppSyncConfig appSyncConfig) {
        return new PrimaryKeyIdMapper<>(Maps.newHashMap());
    }
}
