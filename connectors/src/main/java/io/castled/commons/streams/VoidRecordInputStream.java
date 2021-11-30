package io.castled.commons.streams;

import io.castled.schema.models.Tuple;

import java.nio.file.Path;

public class VoidRecordInputStream implements FileRecordInputStream {
    @Override
    public Tuple readRecord() {
        return null;
    }

    @Override
    public Path getFilePath() {
        return null;
    }
}
