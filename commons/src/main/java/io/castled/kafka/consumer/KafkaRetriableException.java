package io.castled.kafka.consumer;

import lombok.Getter;

public class KafkaRetriableException extends Exception {
    @Getter
    private final long lastProcessedOffset;

    public KafkaRetriableException(long lastProcessedOffset) {
        this.lastProcessedOffset = lastProcessedOffset;
    }
}
