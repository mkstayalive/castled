package io.castled.pipelines.exceptions;

import io.castled.jarvis.taskmanager.exceptions.JarvisRetriableException;

public class PipelineInterruptedException extends JarvisRetriableException {

    public PipelineInterruptedException() {
        super("Pipeline interrupted by shutdown. Task will be retried");

    }
}
