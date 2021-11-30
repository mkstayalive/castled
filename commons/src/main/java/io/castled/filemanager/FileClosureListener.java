package io.castled.filemanager;

import java.nio.file.Path;

public interface FileClosureListener {

    void onFileClosure(Path file);
}
