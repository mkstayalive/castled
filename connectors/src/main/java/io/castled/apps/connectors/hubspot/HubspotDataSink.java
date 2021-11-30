package io.castled.apps.connectors.hubspot;

import com.google.common.collect.Maps;
import io.castled.ObjectRegistry;
import io.castled.apps.DataSink;
import io.castled.apps.OAuthAppConfig;
import io.castled.apps.connectors.hubspot.client.HubspotRestClient;
import io.castled.apps.connectors.hubspot.client.dtos.HubspotObject;
import io.castled.apps.connectors.hubspot.objectsinks.HubspotObjectSink;
import io.castled.apps.connectors.hubspot.schemaMappers.HubspotPropertySchemaMapper;
import io.castled.apps.models.DataSinkRequest;
import io.castled.apps.models.PrimaryKeyIdMapper;
import io.castled.commons.models.AppSyncMode;
import io.castled.commons.models.AppSyncStats;
import io.castled.commons.models.ObjectIdAndMessage;
import io.castled.exceptions.NonThrowingFunction;
import io.castled.schema.SchemaMapper;
import io.castled.schema.models.Message;
import io.castled.schema.models.RecordSchema;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class HubspotDataSink implements DataSink {

    private long skippedRecords = 0;
    private HubspotObjectSink hubspotObjectSink;

    @Override
    public void syncRecords(DataSinkRequest dataSinkRequest) throws Exception {

        Message message;
        HubspotStandardObject hubspotStandardObject = HubspotStandardObject.fromName(dataSinkRequest.getAppSyncConfig().getObject().getObjectName());
        this.hubspotObjectSink = new HubspotObjectSink((OAuthAppConfig) dataSinkRequest.getExternalApp().getConfig(), dataSinkRequest.getErrorOutputStream(),
                hubspotStandardObject);

        PrimaryKeyIdMapper<String> primaryKeyObjectIdMapper = createPrimaryIdMapper(hubspotStandardObject,
                (OAuthAppConfig) dataSinkRequest.getExternalApp().getConfig(), dataSinkRequest.getPrimaryKeys(),
                dataSinkRequest.getObjectSchema());
        while ((message = dataSinkRequest.getMessageInputStream().readMessage()) != null) {
            this.writeMessage(message, dataSinkRequest, hubspotObjectSink, primaryKeyObjectIdMapper);
        }
        hubspotObjectSink.flushRecords();
    }

    @Override
    public AppSyncStats getSyncStats() {
        return Optional.ofNullable(hubspotObjectSink)
                .map(HubspotObjectSink::getSyncStats).map(statsRef -> new AppSyncStats(statsRef.getRecordsProcessed(),
                        statsRef.getOffset(), skippedRecords))
                .orElse(new AppSyncStats(0, 0, 0));
    }

    private PrimaryKeyIdMapper<String> createPrimaryIdMapper(HubspotStandardObject hubspotStandardObject,
                                                             OAuthAppConfig appConfig, List<String> primaryKeys,
                                                             RecordSchema objectSchema) {
        HubspotRestClient hubspotRestClient = new HubspotRestClient(appConfig.getOAuthToken(),
                appConfig.getClientConfig());
        Map<List<Object>, String> objectIds = Maps.newHashMap();
        hubspotRestClient.consumeObjects(primaryKeys, hubspotStandardObject.getObjectUrl(), (object ->
                objectIds.put(primaryKeyValues(object, primaryKeys, objectSchema), object.getId())));
        return new PrimaryKeyIdMapper<>(objectIds);
    }

    private List<Object> primaryKeyValues(HubspotObject object, List<String> primaryKeys, RecordSchema objectSchema) {
        SchemaMapper schemaMapper = ObjectRegistry.getInstance(HubspotPropertySchemaMapper.class);
        return primaryKeys.stream().map(new NonThrowingFunction<>(primaryKey ->
                schemaMapper.transformValue(object.getProperties().get(primaryKey),
                        objectSchema.getSchema(primaryKey)))).collect(Collectors.toList());
    }


    private void writeMessage(Message message, DataSinkRequest dataSinkRequest,
                              HubspotObjectSink hubspotObjectSink, PrimaryKeyIdMapper<String> primaryKeyObjectIdMapper) throws Exception {

        List<Object> primaryKeyValues = dataSinkRequest.getPrimaryKeys().stream().map(pk -> message.getRecord().getValue(pk))
                .collect(Collectors.toList());
        if (dataSinkRequest.getAppSyncConfig().getMode() == AppSyncMode.INSERT) {
            hubspotObjectSink.writeRecord(new ObjectIdAndMessage(null, message));
            return;
        }
        String objectId = primaryKeyObjectIdMapper.getObjectId(primaryKeyValues);
        if (dataSinkRequest.getAppSyncConfig().getMode() == AppSyncMode.UPDATE && objectId == null) {
            skippedRecords++;
            return;
        }
        hubspotObjectSink.writeRecord(new ObjectIdAndMessage(objectId, message));
    }
}
