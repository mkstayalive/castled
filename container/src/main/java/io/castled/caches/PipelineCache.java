package io.castled.caches;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.cache.CastledCache;
import io.castled.daos.PipelineDAO;
import io.castled.models.Pipeline;
import io.castled.pubsub.MessageListener;
import io.castled.pubsub.MessageSubscriber;
import io.castled.pubsub.MessageType;
import io.castled.pubsub.registry.Message;
import io.castled.pubsub.registry.PipelineUpdatedMessage;
import io.castled.utils.TimeUtils;
import org.jdbi.v3.core.Jdbi;

@Singleton
public class PipelineCache extends CastledCache<Long, Pipeline> implements MessageListener {

    @Inject
    public PipelineCache(Jdbi jdbi, MessageSubscriber messageSubscriber) {
        super(TimeUtils.hoursToMillis(3), 1000,
                (pipelineId) -> jdbi.onDemand(PipelineDAO.class).getActivePipeline(pipelineId), false);
        messageSubscriber.subscribe(MessageType.WAREHOUSE_UPDATED, this);
    }

    @Override
    public void handleMessage(Message message) {
        if (message.getType().equals(MessageType.PIPELINE_UPDATED)) {
            PipelineUpdatedMessage pipelineUpdatedMessage = (PipelineUpdatedMessage) message;
            this.invalidate(pipelineUpdatedMessage.getPipelineId());
        }

    }
}
