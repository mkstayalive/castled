package io.castled.filestorage;

import java.io.File;

public interface ObjectStoreClient {

    void uploadFile(String key, File file) throws ObjectStoreException;
}
