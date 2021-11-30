package io.castled.core;

import com.google.common.collect.Sets;
import io.castled.schema.models.Message;
import io.castled.schema.models.MessageOffsetSupplier;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class CastledOffsetQueue<T extends MessageOffsetSupplier> extends CastledBlockingQueue<T> {

    private final Set<Long> pendingMessageIds = Sets.newConcurrentHashSet();
    private long lastBufferedMessageId = 0;

    public CastledOffsetQueue(Consumer<T> consumer, int parallelism, int maxCapacity, boolean exitOnError) {
        super(consumer, parallelism, maxCapacity, exitOnError);
    }

    public void writePayload(T message, int timeout, TimeUnit timeUnit) throws TimeoutException {
        this.lastBufferedMessageId = message.getOffset();
        this.pendingMessageIds.add(message.getOffset());
        super.writePayload(message, timeout, timeUnit);

    }

    public Consumer<T> decorateConsumer(Consumer<T> consumer) {
        return (message -> {
            consumer.accept(message);
            pendingMessageIds.remove(message.getOffset());
        });
    }

    public long getProcessedOffset() {
        try {
            long currentMinPendingId = Collections.min(pendingMessageIds);
            return currentMinPendingId - 1;
        } catch (NoSuchElementException e) {
            return lastBufferedMessageId;
        }
    }

}
