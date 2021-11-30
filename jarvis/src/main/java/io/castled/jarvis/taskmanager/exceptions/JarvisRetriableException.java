package io.castled.jarvis.taskmanager.exceptions;


public class JarvisRetriableException extends RuntimeException {

    public JarvisRetriableException(String message, Throwable cause) {
        super(message, cause);
    }

    public JarvisRetriableException(String message) {
        super(message);
    }

    public JarvisRetriableException(Throwable cause) {
        super(cause);
    }
}
