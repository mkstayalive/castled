package io.castled.kafka.producer;

import com.google.common.collect.Maps;
import io.castled.exceptions.CastledException;
import io.castled.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import java.util.Map;

@Slf4j
public class CastledKafkaProducer implements AutoCloseable {

    private final KafkaProducer<byte[], byte[]> kafkaProducer;

    @Slf4j
    private static class KafkaCallbackWrapper implements Callback {

        private final CastledProducerCallback castledProducerCallback;

        public KafkaCallbackWrapper(CastledProducerCallback castledProducerCallback) {
            this.castledProducerCallback = castledProducerCallback;
        }

        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (castledProducerCallback == null) {
                return;
            }
            if (exception != null) {
                castledProducerCallback.onFailure(metadata, exception);
                log.error("Failed to publish records for topic {} and partition {}", metadata.topic(), metadata.partition(), exception);
                return;
            }
            castledProducerCallback.onSuccess(metadata);
        }
    }

    public CastledKafkaProducer(KafkaProducerConfiguration producerConfiguration) {
        Map<String, Object> producerProps = Maps.newHashMap();

        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerConfiguration.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());

        //these give strict delivery guarantees for kafka
        producerProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
        producerProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        producerProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");

        producerProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 128 * 1024 * 1024);
        producerProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 32 * 1024 * 1024L);
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 512 * 1024);

        producerProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, (int) TimeUtils.minutesToMillis(15));
        producerProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, Long.MAX_VALUE);
        producerProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, (int) TimeUtils.minutesToMillis(20));
        //override defaults
        producerProps.putAll(producerConfiguration.getProps());

        kafkaProducer = new KafkaProducer<>(producerProps);
    }

    public void publish(ProducerRecord<byte[], byte[]> producerRecord) {
        publish(producerRecord, null);
    }

    public void publish(ProducerRecord<byte[], byte[]> producerRecord, CastledProducerCallback castledProducerCallback) {
        this.kafkaProducer.send(producerRecord, new CastledKafkaProducer.KafkaCallbackWrapper(castledProducerCallback));
    }

    public void publishSync(ProducerRecord<byte[], byte[]> producerRecord) throws CastledException {
        try {
            this.kafkaProducer.send(producerRecord).get();
        } catch (Exception e) {
            log.error("Publish sync failed for topic {}", producerRecord.topic(), e);
            throw new CastledException(e.getMessage());
        }
    }

    public void flush() {
        this.kafkaProducer.flush();
    }

    @Override
    public void close() {
        this.kafkaProducer.close();
    }

}
