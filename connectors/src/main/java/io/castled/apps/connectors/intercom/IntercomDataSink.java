package io.castled.apps.connectors.intercom;

import com.google.inject.Inject;
import io.castled.apps.DataSink;
import io.castled.apps.models.DataSinkRequest;
import io.castled.apps.models.GenericSyncObject;
import io.castled.apps.models.PrimaryKeyIdMapper;
import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.commons.models.AppSyncMode;
import io.castled.commons.models.AppSyncStats;
import io.castled.schema.models.Message;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked"})
public class IntercomDataSink implements DataSink {

    private final Map<IntercomObject, IntercomObjectSink> intercomObjectSinks;

    private long skippedRecords = 0;

    private volatile IntercomObjectSink intercomObjectSink;

    @Inject
    public IntercomDataSink(Map<IntercomObject, IntercomObjectSink> intercomObjectSinks) {
        this.intercomObjectSinks = intercomObjectSinks;
    }

    @Override
    public void syncRecords(DataSinkRequest dataSinkRequest) throws Exception {

        GenericSyncObject intercomSyncObject = (GenericSyncObject) dataSinkRequest.getAppSyncConfig().getObject();
        IntercomObject intercomObject = IntercomObject.getObjectByName(intercomSyncObject.getObjectName());
        this.intercomObjectSink =
                this.intercomObjectSinks.get(intercomObject).initialize(intercomObject, dataSinkRequest.getAppSyncConfig(),
                        (IntercomAppConfig) dataSinkRequest.getExternalApp().getConfig(), dataSinkRequest.getErrorOutputStream(),
                        dataSinkRequest.getPrimaryKeys());
        Message message;
        while ((message = dataSinkRequest.getMessageInputStream().readMessage()) != null) {
            if (!this.writeRecord(message, dataSinkRequest.getAppSyncConfig(), intercomObjectSink,
                    dataSinkRequest.getPrimaryKeys())) {
                skippedRecords++;
            }
        }
        intercomObjectSink.flushRecords();
    }

    @Override
    public AppSyncStats getSyncStats() {
        return Optional.ofNullable(intercomObjectSink).map(IntercomObjectSink::getSyncStats).
                map(statsRef -> new AppSyncStats(statsRef.getRecordsProcessed(),
                        statsRef.getOffset(), skippedRecords)).orElse(new AppSyncStats(0, 0, 0));
    }

    private boolean writeRecord(Message message, AppSyncConfig appSyncConfig,
                                IntercomObjectSink intercomObjectSink, List<String> primaryKeys) {

        List<Object> primaryKeyValues = primaryKeys.stream().map(pk -> message.getRecord().getValue(pk)).collect(Collectors.toList());

        PrimaryKeyIdMapper primaryKeyIdMapper = intercomObjectSink.getPrimaryKeyIdMapper();
        Object objectId = primaryKeyIdMapper.getObjectId(primaryKeyValues);
        if (appSyncConfig.getMode() == AppSyncMode.UPDATE && objectId == null) {
            return false;
        }
        if (objectId == null) {
            intercomObjectSink.createObject(message);
        } else {
            intercomObjectSink.updateObject(objectId, message);
        }
        return true;
    }
}
