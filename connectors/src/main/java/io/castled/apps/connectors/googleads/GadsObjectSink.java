package io.castled.apps.connectors.googleads;

import com.google.ads.googleads.v7.errors.GoogleAdsError;
import com.google.rpc.Status;
import io.castled.ObjectRegistry;
import io.castled.apps.BufferedObjectSink;
import io.castled.commons.errors.CastledError;
import io.castled.commons.models.MessageSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.models.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public abstract class GadsObjectSink extends BufferedObjectSink<Message> {
    private final ErrorOutputStream errorOutputStream;

    protected final AtomicLong processedRecords = new AtomicLong(0);
    protected long lastProcessedMessageId = 0;

    public GadsObjectSink(ErrorOutputStream errorOutputStream) {
        this.errorOutputStream = errorOutputStream;
    }

    protected void handlePartialFailures(List<Message> messages, Status partialFailureError) {

        for (int operationIndex = 0; operationIndex < messages.size(); operationIndex++) {
            GoogleAdsError googleAdsError = getGoogleAdsError(operationIndex, partialFailureError);
            if (googleAdsError != null) {
                CastledError pipelineError = ObjectRegistry.getInstance(GadsErrorParser.class).parseGadsError(googleAdsError);
                this.errorOutputStream.writeFailedRecord(messages.get(operationIndex), pipelineError);
            }
        }
    }

    private GoogleAdsError getGoogleAdsError(long operationIndex, Status partialFailureError) {
        try {
            for (GoogleAdsError error : ObjectRegistry.getInstance(GadsErrorUtils.class)
                    .getErrors(operationIndex, partialFailureError)) {
                return error;
            }
            return null;
        } catch (Throwable e) {
            log.error("Protocol1 buffer exception while getting partial failure errors", e);
            throw new CastledRuntimeException(e);
        }
    }

    public MessageSyncStats getSyncStats() {
        return new MessageSyncStats(processedRecords.get(), lastProcessedMessageId);
    }

    public long getMaxBufferedObjects() {
        return 10000;
    }
}
