package io.castled.caches;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.apps.ExternalApp;
import io.castled.apps.daos.ExternalAppDAO;
import io.castled.cache.CastledCache;
import io.castled.pubsub.MessageListener;
import io.castled.pubsub.MessageSubscriber;
import io.castled.pubsub.MessageType;
import io.castled.pubsub.registry.ExternalAppUpdatedMessage;
import io.castled.pubsub.registry.Message;
import io.castled.utils.TimeUtils;
import org.jdbi.v3.core.Jdbi;

@Singleton
public class ExternalAppCache extends CastledCache<Long, ExternalApp> implements MessageListener {

    @Inject
    public ExternalAppCache(Jdbi jdbi, MessageSubscriber messageSubscriber) {
        super(TimeUtils.hoursToMillis(3), 1000,
                (appId) -> jdbi.onDemand(ExternalAppDAO.class).getExternalApp(appId), false);
        messageSubscriber.subscribe(MessageType.EXTERNAL_APP_UPDATED, this);
    }

    @Override
    public void handleMessage(Message message) {
        if (message.getType().equals(MessageType.EXTERNAL_APP_UPDATED)) {
            ExternalAppUpdatedMessage externalAppUpdatedMessage = (ExternalAppUpdatedMessage) message;
            this.invalidate(externalAppUpdatedMessage.getAppId());
        }
    }
}