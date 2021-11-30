package io.castled.jarvis.taskmanager.exceptions;

public class JarvisRuntimeException extends RuntimeException {


    public JarvisRuntimeException(String message) {
        super(message);
    }

    public JarvisRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
