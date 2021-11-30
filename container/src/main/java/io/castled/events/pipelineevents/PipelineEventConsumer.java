package io.castled.events.pipelineevents;

import com.google.inject.Inject;
import io.castled.constants.KafkaApplicationConstants;
import io.castled.kafka.KafkaApplicationConfig;
import io.castled.kafka.consumer.BaseKafkaConsumer;
import io.castled.kafka.consumer.KafkaConsumerConfiguration;
import io.castled.kafka.consumer.KafkaRetriableException;
import io.castled.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;
import java.util.Map;

@Slf4j
public class PipelineEventConsumer extends BaseKafkaConsumer {

    private final Map<PipelineEventType, PipelineEventsHandler> eventHandlers;

    @Inject
    public PipelineEventConsumer(KafkaApplicationConfig kafkaApplicationConfig,
                                 Map<PipelineEventType, PipelineEventsHandler> eventHandlers) {
        super(KafkaConsumerConfiguration.builder().bootstrapServers(kafkaApplicationConfig.getBootstrapServers())
                .consumerGroup(KafkaApplicationConstants.PIPELINE_EVENTS_CONSUMER_GRP).topic(KafkaApplicationConstants.PIPELINE_EVENTS_TOPIC)
                .retryOnUnhandledFailures(false).build());
        this.eventHandlers = eventHandlers;
    }

    @Override
    public long processRecords(List<ConsumerRecord<byte[], byte[]>> consumerRecords) throws Exception {
        long offset = -1;
        for (ConsumerRecord<byte[], byte[]> consumerRecord : consumerRecords) {
            try {
                PipelineEvent pipelineEvent = JsonUtils.byteArrayToObject(consumerRecord.value(), PipelineEvent.class);
                this.eventHandlers.get(pipelineEvent.getEventType()).handlePipelineEvent(pipelineEvent);
                offset = consumerRecord.offset();
            } catch (Exception e) {
                log.error("Pipeline event consumption failed", e);
                throw e;
            }
        }
        return offset;
    }
}
