package io.castled.exceptions.pipeline;

import io.castled.exceptions.CastledException;
import lombok.Getter;

@Getter
public class PipelineExecutionException extends CastledException {

    private final Long pipelineId;
    private final PipelineError pipelineError;
    private final String errorMessage;

    public PipelineExecutionException(Long pipelineId, PipelineError pipelineError, String errorMessage) {
        super(String.format("Pipeline %d failed to run with error %s", pipelineId, errorMessage));
        this.pipelineId = pipelineId;
        this.pipelineError = pipelineError;
        this.errorMessage = errorMessage;
    }
}
