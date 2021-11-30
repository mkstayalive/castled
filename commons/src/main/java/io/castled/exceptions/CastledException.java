package io.castled.exceptions;

public class CastledException extends Exception {

    public CastledException(String message) {
        super(message);
    }

    public CastledException(Throwable cause) {
        super(cause);
    }

    public CastledException(String message, Throwable cause) {
        super(message, cause);
    }
}
