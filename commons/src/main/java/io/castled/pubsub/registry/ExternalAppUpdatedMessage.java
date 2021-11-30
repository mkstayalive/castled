package io.castled.pubsub.registry;

import io.castled.pubsub.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExternalAppUpdatedMessage extends Message {

    private Long appId;

    public ExternalAppUpdatedMessage(Long appId) {
        super(MessageType.EXTERNAL_APP_UPDATED);
        this.appId = appId;
    }
}
