package io.castled.filestorage;

import io.castled.exceptions.CastledException;

public class ObjectStoreException extends CastledException {

    public ObjectStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
