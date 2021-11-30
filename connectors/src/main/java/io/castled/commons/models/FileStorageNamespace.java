package io.castled.commons.models;

import lombok.Getter;

public enum FileStorageNamespace {
    PIPELINE_UNLOADS("pipeline-unloads"),
    PIPELINE_FAILED_RECORDS("pipeline-failed-records"),
    PIPELINE_ERRORS("pipeline-errors");

    FileStorageNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Getter
    private final String namespace;
}
