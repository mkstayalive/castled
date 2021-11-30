package io.castled.apps.connectors.sendgrid;

import io.castled.apps.DataSink;
import io.castled.apps.models.DataSinkRequest;
import io.castled.commons.models.AppSyncStats;
import io.castled.schema.models.Message;

import java.util.Optional;

public class SendgridDataSink implements DataSink {

    private volatile SendgridContactSink sendgridContactSink;

    @Override
    public void syncRecords(DataSinkRequest dataSinkRequest) throws Exception {
        this.sendgridContactSink = new SendgridContactSink((SendgridAppConfig) dataSinkRequest.getExternalApp().getConfig(),
                (SendgridAppSyncConfig)dataSinkRequest.getAppSyncConfig(), dataSinkRequest.getErrorOutputStream());
        Message msg;
        while ((msg = dataSinkRequest.getMessageInputStream().readMessage()) != null) {
            this.sendgridContactSink.writeRecord(msg);
        }
        this.sendgridContactSink.flushRecords();
    }

    @Override
    public AppSyncStats getSyncStats() {
        return Optional.ofNullable(sendgridContactSink).map(sinkRef -> sinkRef.getSyncStats())
                .orElse(new AppSyncStats(0, 0, 0));
    }
}
