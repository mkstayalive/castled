package io.castled.commons.streams;

import java.nio.file.Path;

public interface FileRecordInputStream extends RecordInputStream {

    Path getFilePath();
}
