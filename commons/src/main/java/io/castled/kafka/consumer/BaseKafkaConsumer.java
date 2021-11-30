package io.castled.kafka.consumer;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public abstract class BaseKafkaConsumer implements Runnable {

    private final KafkaConsumer<byte[], byte[]> kafkaConsumer;
    private final AtomicReference<ConsumerState> consumerState = new AtomicReference<>(ConsumerState.RUNNING);
    private final KafkaConsumerConfiguration kafkaConsumerConfiguration;

    public BaseKafkaConsumer(KafkaConsumerConfiguration consumerConfiguration) {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerConfiguration.getConsumerGroup());
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerConfiguration.getBootstrapServers());
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
        consumerProps.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60 * 1000);
        consumerProps.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 8 * 1024 * 1024); // 8 MB
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000);

        kafkaConsumer = new KafkaConsumer<>(consumerProps);
        this.kafkaConsumerConfiguration = consumerConfiguration;
    }

    @Override
    public void run() {
        this.kafkaConsumer.subscribe(Collections.singleton(kafkaConsumerConfiguration.getTopic()));
        while (consumerState.get() == ConsumerState.RUNNING) {
            ConsumerRecords<byte[], byte[]> records = this.kafkaConsumer.poll(Duration.ofSeconds(10));
            for (TopicPartition topicPartition : records.partitions()) {
                this.processPartition(topicPartition, records.records(topicPartition));
            }
        }
    }

    private void processPartition(TopicPartition topicPartition, List<ConsumerRecord<byte[], byte[]>> partitionRecords) {
        try {
            long lastProcessedOffset = this.processRecords(partitionRecords);
            if (lastProcessedOffset != -1) {
                this.kafkaConsumer.commitSync(ImmutableMap.of(topicPartition, new OffsetAndMetadata(lastProcessedOffset + 1)));
            }
        } catch (KafkaRetriableException e) {
            if (e.getLastProcessedOffset() != -1) {
                this.kafkaConsumer.seek(topicPartition, e.getLastProcessedOffset() + 1);
            } else {
                this.kafkaConsumer.seek(topicPartition, partitionRecords.get(0).offset());
            }

        } catch (Exception e) {
            log.error("Failed to process records for topic {} and partition {}", topicPartition.topic(), topicPartition.partition(), e);
            if (kafkaConsumerConfiguration.isRetryOnUnhandledFailures()) {
                this.kafkaConsumer.seek(topicPartition, partitionRecords.get(0).offset());
            }
        }
    }

    public abstract long processRecords(List<ConsumerRecord<byte[], byte[]>> partitionRecords) throws Exception;

    public void stop() throws Exception {
        this.consumerState.set(ConsumerState.TERMINATED);
    }
}
