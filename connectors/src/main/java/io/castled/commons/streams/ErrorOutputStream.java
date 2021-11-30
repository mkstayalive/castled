package io.castled.commons.streams;

import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.CastledErrorTracker;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.models.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class ErrorOutputStream {

    private final RecordOutputStream recordOutputStream;
    private final CastledErrorTracker castledErrorTracker;

    @Getter
    private final AtomicLong failedRecords = new AtomicLong(0);
    @Getter
    private volatile Long firstFailedMessageId;

    public ErrorOutputStream(RecordOutputStream recordOutputStream, CastledErrorTracker castledErrorTracker) {
        this.recordOutputStream = recordOutputStream;
        this.castledErrorTracker = castledErrorTracker;
    }

    public void writeFailedRecord(Message message, CastledError pipelineError) {
        try {
            if (firstFailedMessageId == null) {
                firstFailedMessageId = message.getOffset();
            }
            this.failedRecords.incrementAndGet();
            this.castledErrorTracker.writeError(message.getRecord(), pipelineError);
            this.recordOutputStream.writeRecord(message.getRecord());
        } catch (Exception e) {
            log.error(String.format("Write failed record failed for error %s", pipelineError.description()));
            throw new CastledRuntimeException(e);
        }
    }

    public void flushFailedRecords() throws Exception {
        this.recordOutputStream.flush();
        this.castledErrorTracker.flushErrors();
    }

}
