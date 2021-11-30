package io.castled.pubsub;

import io.castled.pubsub.registry.Message;

public interface MessageListener {
    void handleMessage(Message message);
}
