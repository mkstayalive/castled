package io.castled.pubsub;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.constants.CommonKafkaConstants;
import io.castled.kafka.KafkaApplicationConfig;
import io.castled.kafka.consumer.BaseKafkaConsumer;
import io.castled.kafka.consumer.KafkaConsumerConfiguration;
import io.castled.pubsub.registry.Message;
import io.castled.utils.JsonUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;
import java.util.UUID;

@Singleton
public class PubSubConsumer extends BaseKafkaConsumer {

    private final MessageSubscriber messageSubscriber;

    @Inject
    public PubSubConsumer(KafkaApplicationConfig kafkaApplicationConfig, MessageSubscriber messageSubscriber) {
        super(KafkaConsumerConfiguration.builder().bootstrapServers(kafkaApplicationConfig.getBootstrapServers())
                .consumerGroup(UUID.randomUUID().toString()).topic(CommonKafkaConstants.PUBSUB_MESSAGE_TOPIC)
                .retryOnUnhandledFailures(false).build());
        this.messageSubscriber = messageSubscriber;
    }

    @Override
    public long processRecords(List<ConsumerRecord<byte[], byte[]>> consumerRecords) throws Exception {
        long offset = -1;
        for (ConsumerRecord<byte[], byte[]> consumerRecord : consumerRecords) {
            Message message = JsonUtils.byteArrayToObject(consumerRecord.value(), Message.class);
            this.messageSubscriber.consumeMessage(message);
            offset = consumerRecord.offset();
        }
        return offset;

    }
}
