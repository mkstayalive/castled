package io.castled.kafka.producer;

import com.google.common.collect.Maps;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class KafkaProducerConfiguration {

    private String bootstrapServers;

    @Builder.Default
    // optional property overrides
    private Map<String, Object> props = Maps.newHashMap();
}
