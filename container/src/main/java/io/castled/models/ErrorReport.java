package io.castled.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorReport {

    private Long id;
    private Long pipelineId;
    private Long pipelineRunId;
    private String report;
}
