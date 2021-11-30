package io.castled.caches;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.cache.CastledCache;
import io.castled.daos.UsersDAO;
import io.castled.models.users.User;
import io.castled.pubsub.MessageListener;
import io.castled.pubsub.MessageSubscriber;
import io.castled.pubsub.MessageType;
import io.castled.pubsub.registry.Message;
import io.castled.pubsub.registry.UserUpdatedMessage;
import io.castled.utils.TimeUtils;
import org.jdbi.v3.core.Jdbi;

@Singleton
public class UsersCache extends CastledCache<Long, User> implements MessageListener {
    @Inject
    public UsersCache(Jdbi jdbi, MessageSubscriber messageSubscriber) {
        super(TimeUtils.hoursToMillis(3), 1000,
                userId -> jdbi.onDemand(UsersDAO.class).getUser(userId), false);
        messageSubscriber.subscribe(MessageType.USER_UPDATED, this);
    }

    @Override
    public void handleMessage(Message message) {
        if (message.getType().equals(MessageType.USER_UPDATED)) {
            UserUpdatedMessage userUpdatedMessage = (UserUpdatedMessage) message;
            this.invalidate(userUpdatedMessage.getUserId());
        }
    }
}
