package io.castled.exceptions;

public class CastledRuntimeException extends RuntimeException {

    public CastledRuntimeException(String message) {
        super(message);
    }

    public CastledRuntimeException(Throwable cause) {
        super(cause);
    }
}

