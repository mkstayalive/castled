package io.castled.jarvis.taskmanager.exceptions;

public class JarvisException extends Exception {

    public JarvisException(String message, Throwable cause) {
        super(message, cause);
    }

    public JarvisException(String message) {
        super(message);
    }

    public JarvisException(Throwable cause) {
        super(cause);
    }
}
