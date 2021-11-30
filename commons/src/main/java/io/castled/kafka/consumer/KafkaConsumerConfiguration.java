package io.castled.kafka.consumer;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class KafkaConsumerConfiguration {
    private String consumerGroup;
    private Map<String, Object> props;
    private String topic;
    private String bootstrapServers;
    private boolean retryOnUnhandledFailures;
}
