package io.castled.jarvis.taskmanager.models;

import io.castled.kafka.producer.CastledKafkaProducer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JarvisKafkaConfig {

    private String bootstrapServers;

    //optional, so that the client can reuse the kafka producer they have already created
    private CastledKafkaProducer castledKafkaProducer;

    private int consumerCount;


}
