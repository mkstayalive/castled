package io.castled.pubsub;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import io.castled.pubsub.registry.Message;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
public class MessageSubscriber {
    private final Map<MessageType, List<MessageListener>> messageListeners = Maps.newConcurrentMap();

    public void subscribe(MessageType messageType, MessageListener messageListener) {
        this.messageListeners.computeIfAbsent(messageType, key -> new CopyOnWriteArrayList<>());
        this.messageListeners.get(messageType).add(messageListener);
    }

    public void consumeMessage(Message message) {
        for (MessageListener messageListener : messageListeners.get(message.getType())) {
            messageListener.handleMessage(message);
        }
    }
}
