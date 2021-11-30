package io.castled.exceptions.pipeline;

import lombok.Getter;

public enum PipelineError {
    INSUFFICIENT_WAREHOUSE_PREVILEGES(PipelineErrorType.USER_ACTIION_REQUIRED),
    CONNECTION_FAILED_INTERMITTENT(PipelineErrorType.INTERMITTENT),
    UNKNOWN_ERROR(PipelineErrorType.INTERNAL);

    @Getter
    private final PipelineErrorType pipelineErrorType;

    PipelineError(PipelineErrorType pipelineErrorType) {
        this.pipelineErrorType = pipelineErrorType;

    }
}
