package io.castled.commons.streams;

import io.castled.schema.models.Tuple;

public interface RecordInputStream extends AutoCloseable {

    Tuple readRecord() throws Exception;

    default void close() throws Exception {

    }
}
