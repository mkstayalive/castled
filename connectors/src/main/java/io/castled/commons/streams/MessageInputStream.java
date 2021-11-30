package io.castled.commons.streams;

import io.castled.schema.models.Message;

public interface MessageInputStream extends AutoCloseable{

    Message readMessage() throws Exception;
}
