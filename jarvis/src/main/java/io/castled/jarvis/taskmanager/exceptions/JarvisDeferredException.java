package io.castled.jarvis.taskmanager.exceptions;


import lombok.Getter;

@Getter
public class JarvisDeferredException extends RuntimeException {

    private final long deferredTill;

    public JarvisDeferredException(Long deferredTill, String message) {
        super(message);
        this.deferredTill = deferredTill;
    }
}

