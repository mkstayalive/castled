package io.castled.apps.connectors.mixpanel;

import io.castled.apps.BufferedObjectSink;
import io.castled.commons.models.MessageSyncStats;
import io.castled.schema.models.Message;

import java.util.concurrent.TimeoutException;


public abstract class MixpanelObjectSink<Message> extends BufferedObjectSink<Message> {

    public abstract MessageSyncStats getSyncStats();
}
