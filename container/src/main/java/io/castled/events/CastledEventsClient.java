package io.castled.events;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.constants.KafkaApplicationConstants;
import io.castled.events.pipelineevents.PipelineEvent;
import io.castled.kafka.producer.CastledKafkaProducer;
import io.castled.utils.JsonUtils;
import org.apache.kafka.clients.producer.ProducerRecord;

@Singleton
public class CastledEventsClient {

    private final CastledKafkaProducer castledKafkaProducer;

    @Inject
    public CastledEventsClient(CastledKafkaProducer castledKafkaProducer) {
        this.castledKafkaProducer = castledKafkaProducer;
    }

    public void publishPipelineEvent(PipelineEvent pipelineEvent) {
        ProducerRecord<byte[], byte[]> producerRecord = new ProducerRecord<>(KafkaApplicationConstants.PIPELINE_EVENTS_TOPIC, null,
                String.valueOf(pipelineEvent.getPipelineId()).getBytes(),
                JsonUtils.objectToString(pipelineEvent).getBytes());
        this.castledKafkaProducer.publish(producerRecord);
    }

}
