package io.castled;

import com.google.inject.Inject;
import io.castled.events.pipelineevents.PipelineEventConsumer;
import io.castled.kafka.consumer.ConsumerUtils;
import io.castled.pubsub.PubSubConsumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CastledAppManager {

    private final PipelineEventConsumer pipelineEventConsumer;
    private final PubSubConsumer pubSubConsumer;

    @Inject
    public CastledAppManager(PipelineEventConsumer pipelineEventConsumer,
                             PubSubConsumer pubSubConsumer) {
        this.pipelineEventConsumer = pipelineEventConsumer;
        this.pubSubConsumer = pubSubConsumer;
    }

    public void initializeAppComponents() {
        ConsumerUtils.runKafkaConsumer(1, "pipeline_events", pipelineEventConsumer);
        ConsumerUtils.runKafkaConsumer(1, "pubsub", pubSubConsumer);
    }

}
