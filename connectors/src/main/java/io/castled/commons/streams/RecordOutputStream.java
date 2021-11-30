package io.castled.commons.streams;


import io.castled.schema.models.Tuple;

public interface RecordOutputStream {

    void writeRecord(Tuple record) throws Exception;

    void flush() throws Exception;

}
