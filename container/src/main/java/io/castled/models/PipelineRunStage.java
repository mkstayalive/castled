package io.castled.models;

public enum PipelineRunStage {
    RUN_TRIGGERED,
    RECORDS_POLLED,
    RECORDS_SYNCED,
    FAILURE_RECORDS_PROCESSED;

    public boolean recordsPolled() {
        return this == RECORDS_POLLED || this == RECORDS_SYNCED || this == FAILURE_RECORDS_PROCESSED;
    }
}
