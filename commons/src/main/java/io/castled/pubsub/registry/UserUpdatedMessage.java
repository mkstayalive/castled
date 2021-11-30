package io.castled.pubsub.registry;

import io.castled.pubsub.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdatedMessage extends Message {

    private Long userId;

    public UserUpdatedMessage(Long userId) {
        super(MessageType.USER_UPDATED);
        this.userId = userId;
    }
}