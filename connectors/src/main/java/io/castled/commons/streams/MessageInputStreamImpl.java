package io.castled.commons.streams;

import io.castled.schema.models.Message;
import io.castled.schema.models.Tuple;

public class MessageInputStreamImpl implements MessageInputStream {

    private final RecordInputStream recordInputStream;
    private long count = 1;
    private final long startingOffset;

    public MessageInputStreamImpl(RecordInputStream recordInputStream, long startingOffset) {
        this.recordInputStream = recordInputStream;
        this.startingOffset = startingOffset;
    }

    public Message readMessage() throws Exception {
        Tuple record = this.recordInputStream.readRecord();
        if (record == null) {
            return null;
        }
        while (count <= startingOffset) {
            this.recordInputStream.readRecord();
            count++;
        }
        return new Message(count++, record);
    }

    @Override
    public void close() throws Exception {
        this.recordInputStream.close();
    }
}
