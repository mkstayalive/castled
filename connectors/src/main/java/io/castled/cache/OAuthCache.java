package io.castled.cache;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.oauth.OAuthDAO;
import io.castled.oauth.OAuthDetails;
import io.castled.pubsub.MessageListener;
import io.castled.pubsub.MessageSubscriber;
import io.castled.pubsub.MessageType;
import io.castled.pubsub.registry.Message;
import io.castled.pubsub.registry.OAuthDetailsUpdatedMessage;
import io.castled.utils.TimeUtils;
import org.jdbi.v3.core.Jdbi;

@Singleton
public class OAuthCache extends CastledCache<Long, OAuthDetails> implements MessageListener {

    @Inject
    public OAuthCache(Jdbi jdbi, MessageSubscriber messageSubscriber) {
        super(TimeUtils.hoursToMillis(3), 1000,
                tokenId -> jdbi.onDemand(OAuthDAO.class).getOAuthDetails(tokenId), false);
        messageSubscriber.subscribe(MessageType.OAUTH_DETAILS_UPDATED, this);
    }

    @Override
    public void handleMessage(Message message) {
        if (message.getType().equals(MessageType.OAUTH_DETAILS_UPDATED)) {
            OAuthDetailsUpdatedMessage oAuthUpdatedMessage = (OAuthDetailsUpdatedMessage) message;
            this.invalidate(oAuthUpdatedMessage.getOauthId());
        }
    }
}
