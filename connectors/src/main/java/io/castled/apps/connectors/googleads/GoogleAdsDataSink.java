package io.castled.apps.connectors.googleads;

import io.castled.apps.DataSink;
import io.castled.apps.models.DataSinkRequest;
import io.castled.commons.models.AppSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.models.Message;

import java.util.Optional;

public class GoogleAdsDataSink implements DataSink {

    private GadsObjectSink gadsObjectSink;

    @Override
    public void syncRecords(DataSinkRequest dataSinkRequest) throws Exception {
        Message message;
        this.gadsObjectSink = getObjectSink((GoogleAdsAppSyncConfig) dataSinkRequest.getAppSyncConfig(),
                (GoogleAdsAppConfig) dataSinkRequest.getExternalApp().getConfig(),
                dataSinkRequest.getErrorOutputStream());
        while ((message = dataSinkRequest.getMessageInputStream().readMessage()) != null) {
            gadsObjectSink.writeRecord(message);
        }
        gadsObjectSink.flushRecords();

    }

    private GadsObjectSink getObjectSink(GoogleAdsAppSyncConfig mappingConfig,
                                         GoogleAdsAppConfig googleAdsAppConfig,
                                         ErrorOutputStream errorOutputStream) {
        GAdsObjectType gAdsObjectType = GAdsObjectType.valueOf(mappingConfig.getObject().getObjectName());

        switch (gAdsObjectType) {
            case CUSTOMER_MATCH:
                return new CustomerMatchObjectSink(mappingConfig, googleAdsAppConfig, errorOutputStream);
            case CLICK_CONVERSIONS:
            case CALL_CONVERSIONS:
                return new ConversionObjectSink(mappingConfig, googleAdsAppConfig, errorOutputStream);
            default:
                throw new CastledRuntimeException(String.format("Unhandled sync object type %s", gAdsObjectType));
        }
    }

    @Override
    public AppSyncStats getSyncStats() {
        return Optional.ofNullable(gadsObjectSink).map(GadsObjectSink::getSyncStats).
                map(statsRef -> new AppSyncStats(statsRef.getRecordsProcessed(), statsRef.getOffset(), 0)).orElse(new AppSyncStats(0, 0, 0));
    }
}
