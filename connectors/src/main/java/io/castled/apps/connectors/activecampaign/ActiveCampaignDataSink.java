package io.castled.apps.connectors.activecampaign;

import io.castled.apps.DataSink;
import io.castled.apps.models.DataSinkRequest;
import io.castled.apps.models.GenericSyncObject;
import io.castled.commons.models.AppSyncStats;
import io.castled.schema.models.Message;

import java.util.Optional;

public class ActiveCampaignDataSink implements DataSink {

    private ActiveCampaignAudienceSink activeCampaignAudienceSink;

    @Override
    public void syncRecords(DataSinkRequest dataSinkRequest) throws Exception {
        Message message;
        this.activeCampaignAudienceSink = new ActiveCampaignAudienceSink((ActiveCampaignAppConfig) dataSinkRequest.getExternalApp().getConfig(),
                dataSinkRequest.getErrorOutputStream(), (GenericSyncObject) dataSinkRequest.getAppSyncConfig().getObject());
        while ((message = dataSinkRequest.getMessageInputStream().readMessage()) != null) {
            this.activeCampaignAudienceSink.writeRecord(message);
        }
        this.activeCampaignAudienceSink.flushRecords();
    }

    @Override
    public AppSyncStats getSyncStats() {
        return Optional.ofNullable(this.activeCampaignAudienceSink)
                .map(audienceSinkRef -> this.activeCampaignAudienceSink.getSyncStats())
                .map(statsRef -> new AppSyncStats(statsRef.getRecordsProcessed(), statsRef.getOffset(), 0))
                .orElse(new AppSyncStats(0, 0, 0));
    }
}
