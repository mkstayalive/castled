package io.castled.commons.streams;

import io.castled.schema.models.Message;

public interface MessageOutputStream {

    void writeRecord(Message message) throws Exception;

    void flush() throws Exception;
}
