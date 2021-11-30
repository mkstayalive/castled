package io.castled.apps.connectors.mailchimp;

import io.castled.apps.DataSink;
import io.castled.apps.OAuthAppConfig;
import io.castled.apps.models.DataSinkRequest;
import io.castled.commons.models.AppSyncStats;
import io.castled.schema.models.Message;

import java.util.Optional;

public class MailchimpDataSink implements DataSink {

    private MailchimpAudienceSink mailchimpAudienceSink;

    @Override
    public void syncRecords(DataSinkRequest dataSinkRequest) throws Exception {
        Message message;
        this.mailchimpAudienceSink = new MailchimpAudienceSink((OAuthAppConfig) dataSinkRequest.getExternalApp().getConfig(),
                dataSinkRequest.getErrorOutputStream(), (MailchimpAudienceSyncObject) dataSinkRequest.getAppSyncConfig().getObject());
        while ((message = dataSinkRequest.getMessageInputStream().readMessage()) != null) {
            this.mailchimpAudienceSink.writeRecord(message);
        }
        this.mailchimpAudienceSink.flushRecords();
    }

    @Override
    public AppSyncStats getSyncStats() {
        return Optional.ofNullable(this.mailchimpAudienceSink)
                .map(audienceSinkRef -> this.mailchimpAudienceSink.getSyncStats())
                .map(statsRef -> new AppSyncStats(statsRef.getRecordsProcessed(), statsRef.getOffset(), 0))
                .orElse(new AppSyncStats(0, 0, 0));
    }
}
