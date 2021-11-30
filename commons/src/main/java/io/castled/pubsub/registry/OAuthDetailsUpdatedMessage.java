package io.castled.pubsub.registry;

import io.castled.pubsub.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OAuthDetailsUpdatedMessage extends Message {

    private Long oauthId;

    public OAuthDetailsUpdatedMessage(Long oauthId) {
        super(MessageType.OAUTH_DETAILS_UPDATED);
        this.oauthId = oauthId;
    }
}
