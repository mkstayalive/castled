package io.castled.models;

import io.castled.commons.models.PipelineSyncStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PipelineRun {

    private Long id;
    private PipelineRunStatus status;
    private PipelineRunStage stage;
    private Long pipelineId;
    private String failureMessage;
    private PipelineSyncStats pipelineSyncStats;
    private Long processedTs;
    private Long createdTs;
}
