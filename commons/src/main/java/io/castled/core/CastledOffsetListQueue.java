package io.castled.core;

import com.google.common.collect.Sets;
import io.castled.schema.models.MessageOffsetSupplier;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class CastledOffsetListQueue<T extends MessageOffsetSupplier> extends CastledBlockingQueue<List<T>> {
    private final Set<Long> pendingMessageIds = Sets.newConcurrentHashSet();
    private long lastBufferedMessageId = 0;

    public CastledOffsetListQueue(Consumer<List<T>> consumer, int parallelism, int maxCapacity, boolean exitOnError) {
        super(consumer, parallelism, maxCapacity, exitOnError);
    }

    public void writePayload(List<T> messageList, int timeout, TimeUnit timeUnit) throws TimeoutException {
        if (CollectionUtils.isEmpty(messageList)) {
            return;
        }
        this.lastBufferedMessageId = messageList.get(messageList.size() - 1).getOffset();
        messageList.forEach(message -> pendingMessageIds.add(message.getOffset()));
        super.writePayload(messageList, timeout, timeUnit);

    }

    public Consumer<List<T>> decorateConsumer(Consumer<List<T>> consumer) {
        return (messageList -> {
            consumer.accept(messageList);
            messageList.forEach(message -> pendingMessageIds.remove(message.getOffset()));
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
