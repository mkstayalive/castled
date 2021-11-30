package io.castled.apps.connectors.hubspot.objectsinks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.castled.ObjectRegistry;
import io.castled.apps.BufferedObjectSink;
import io.castled.apps.OAuthAppConfig;
import io.castled.apps.connectors.hubspot.HubspotErrorParser;
import io.castled.apps.connectors.hubspot.HubspotStandardObject;
import io.castled.apps.connectors.hubspot.client.HubspotRestClient;
import io.castled.apps.connectors.hubspot.client.dtos.BatchUpdateRequest;
import io.castled.apps.connectors.hubspot.client.dtos.ObjectUpdateRequest;
import io.castled.apps.connectors.hubspot.client.exception.BatchObjectException;
import io.castled.apps.connectors.hubspot.schemaMappers.HubspotApiSchemaMapper;
import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.errorclassifications.UnclassifiedError;
import io.castled.commons.models.MessageSyncStats;
import io.castled.commons.models.ObjectIdAndMessage;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.core.CastledOffsetListQueue;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.IncompatibleValueException;
import io.castled.schema.SchemaMapper;
import io.castled.schema.models.Field;
import io.castled.schema.models.Tuple;
import io.castled.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class HubspotObjectSink extends BufferedObjectSink<ObjectIdAndMessage> {

    private final CastledOffsetListQueue<ObjectIdAndMessage> requestsBuffer =
            new CastledOffsetListQueue<>(new ObjectUpdateConsumer(), 5, 500, true);

    private final AtomicLong processedRecords = new AtomicLong(0);

    private final HubspotRestClient hubspotRestClient;
    private final ErrorOutputStream errorOutputStream;
    private final HubspotStandardObject hubspotStandardObject;

    public HubspotObjectSink(OAuthAppConfig oAuthAppConfig,
                             ErrorOutputStream errorOutputStream, HubspotStandardObject hubspotStandardObject) {

        this.hubspotStandardObject = hubspotStandardObject;
        this.hubspotRestClient = new HubspotRestClient(oAuthAppConfig.getOAuthToken(),
                oAuthAppConfig.getClientConfig());
        this.errorOutputStream = errorOutputStream;
    }

    private class ObjectUpdateConsumer implements Consumer<List<ObjectIdAndMessage>> {

        @Override
        public void accept(List<ObjectIdAndMessage> records) {
            updateRecords(records.stream().filter(record -> record.getId() == null).collect(Collectors.toList()), true);
            updateRecords(records.stream().filter(record -> record.getId() != null).collect(Collectors.toList()), false);
            processedRecords.addAndGet(records.size());
        }

        private void updateRecords(List<ObjectIdAndMessage> records, boolean create) {
            if (CollectionUtils.isEmpty(records)) {
                return;
            }
            List<ObjectUpdateRequest> objectUpdateRequests = records.stream().map(recordRef -> new ObjectUpdateRequest(recordRef.getId(),
                    createObjectProperties(recordRef.getMessage().getRecord()))).collect(Collectors.toList());
            BatchUpdateRequest batchUpdateRequest = new BatchUpdateRequest(objectUpdateRequests);
            try {
                hubspotRestClient.updateObjects(hubspotStandardObject.getObjectUrl(), batchUpdateRequest, create);
            } catch (BatchObjectException e) {
                if (e.getBatchObjectError() == null || e.getBatchObjectError().getCategory() == null) {
                    log.error("Hubspot records update failed", e);
                }
                CastledError pipelineError = ObjectRegistry.getInstance(HubspotErrorParser.class).parseError(e.getBatchObjectError());
                for (ObjectIdAndMessage record : records) {
                    errorOutputStream.writeFailedRecord(record.getMessage(), pipelineError);
                }
            }
        }
    }

    protected Map<String, Object> createObjectProperties(Tuple bufferedRecord) {
        SchemaMapper schemaMapper = ObjectRegistry.getInstance(HubspotApiSchemaMapper.class);
        try {
            Map<String, Object> properties = Maps.newHashMap();
            for (Field field : bufferedRecord.getFields()) {
                if (bufferedRecord.getValue(field.getName()) != null) {
                    properties.put(field.getName(), schemaMapper.transformValue(bufferedRecord.getValue(field.getName()), field.getSchema()));
                } else {
                    properties.put(field.getName(), "");

                }
            }
            return properties;
        } catch (IncompatibleValueException e) {
            //this cannot happen ideally
            throw new CastledRuntimeException(e);
        }
    }


    @Override
    protected void writeRecords(List<ObjectIdAndMessage> records) {
        try {
            requestsBuffer.writePayload(Lists.newArrayList(records), 5, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            log.error("Unable to publish records to records queue", e);
            for (ObjectIdAndMessage record : records) {
                errorOutputStream.writeFailedRecord(record.getMessage(),
                        new UnclassifiedError("Internal error!! Unable to publish records to records queue. Please contact support"));
            }
        }
    }

    public MessageSyncStats getSyncStats() {
        return new MessageSyncStats(processedRecords.get(), requestsBuffer.getProcessedOffset());
    }

    public void flushRecords() throws Exception {
        super.flushRecords();
        requestsBuffer.flush(TimeUtils.minutesToMillis(10));
    }

    @Override
    public long getMaxBufferedObjects() {
        return 10;
    }
}
