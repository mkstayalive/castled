package io.castled.apps.connectors.mixpanel;

import io.castled.apps.DataSink;
import io.castled.apps.models.DataSinkRequest;
import io.castled.commons.models.AppSyncStats;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.models.Message;

import java.util.Optional;

public class MixpanelDataSink implements DataSink {


    private volatile MixpanelObjectSink mixedPanelObjectSink;

    private long skippedRecords = 0;

    @Override
    public void syncRecords(DataSinkRequest dataSinkRequest) throws Exception {

        this.mixedPanelObjectSink = getObjectSink(dataSinkRequest);
        Message message;
        while ((message = dataSinkRequest.getMessageInputStream().readMessage()) != null) {
            this.mixedPanelObjectSink.writeRecord(message);
        }
        this.mixedPanelObjectSink.flushRecords();
    }

    private MixpanelObjectSink getObjectSink(DataSinkRequest dataSinkRequest) {
        MixpanelObjectSink bufferedObjectSink = null;
        MixpanelObject customerIOObject = MixpanelObject
                .getObjectByName(dataSinkRequest.getAppSyncConfig().getObject().getObjectName());
        switch (customerIOObject) {
            case USER_PROFILE:
                bufferedObjectSink = new MixpanelUserProfileSink(dataSinkRequest);
                break;
            case GROUP_PROFILE:
                bufferedObjectSink = new MixpanleGroupProfileSink(dataSinkRequest);
                break;
            case EVENT:
                bufferedObjectSink = new MixpanelEventSink(dataSinkRequest);
                break;
            default:
                throw new CastledRuntimeException(String.format("Invalid object type %s!", customerIOObject.getName()));
        }
        return bufferedObjectSink;
    }

    @Override
    public AppSyncStats getSyncStats() {
        return Optional.ofNullable(this.mixedPanelObjectSink)
                .map(audienceSinkRef -> this.mixedPanelObjectSink.getSyncStats())
                .map(statsRef -> new AppSyncStats(statsRef.getRecordsProcessed(), statsRef.getOffset(), 0))
                .orElse(new AppSyncStats(0, 0, 0));
    }
}
