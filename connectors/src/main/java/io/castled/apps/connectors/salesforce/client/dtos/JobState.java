package io.castled.apps.connectors.salesforce.client.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum JobState {
    OPEN("Open"),

    UPLOAD_COMPLETE("UploadComplete"),

    IN_PROGRESS("InProgress"),

    ABORTED("Aborted"),

    JOB_COMPLETE("JobComplete"),

    FAILED("Failed");

    private final String displayName;

    JobState(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String toJsonValue() {
        return displayName;
    }

    @JsonCreator
    public static JobState fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(v -> v.displayName.equals(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.valueOf(displayName)));
    }
}
