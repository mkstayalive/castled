package io.castled.pubsub;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.constants.CommonKafkaConstants;
import io.castled.kafka.producer.CastledKafkaProducer;
import io.castled.pubsub.registry.Message;
import io.castled.utils.JsonUtils;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.UUID;

@Singleton
public class MessagePublisher {

    private final CastledKafkaProducer castledKafkaProducer;

    @Inject
    public MessagePublisher(CastledKafkaProducer castledKafkaProducer) {
        this.castledKafkaProducer = castledKafkaProducer;
    }

    public void publishMessage(Message message) {
        ProducerRecord<byte[], byte[]> producerRecord = new ProducerRecord<>(CommonKafkaConstants.PUBSUB_MESSAGE_TOPIC, null,
                UUID.randomUUID().toString().getBytes(), JsonUtils.objectToString(message).getBytes());
        this.castledKafkaProducer.publish(producerRecord);
    }
}
