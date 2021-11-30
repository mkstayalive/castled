package io.castled.jarvis.taskmanager.exceptions;

public class JarvisTaskInProgressException extends JarvisException {

    public JarvisTaskInProgressException(String uniqueId) {
        super(String.format("Another task with the unique id %s already in progress", uniqueId));
    }
}
