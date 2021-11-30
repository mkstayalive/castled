package io.castled.encryption;

import io.castled.exceptions.CastledException;

public class EncryptionException extends CastledException {

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
