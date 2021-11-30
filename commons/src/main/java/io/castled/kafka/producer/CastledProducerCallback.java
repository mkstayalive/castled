package io.castled.kafka.producer;

import org.apache.kafka.clients.producer.RecordMetadata;

public interface CastledProducerCallback {

    void onSuccess(RecordMetadata recordMetadata);

    void onFailure(RecordMetadata recordMetadata, Exception e);


}
