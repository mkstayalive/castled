package io.castled.dtos;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.castled.models.Pipeline;
import io.castled.models.PipelineRun;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Data
public class PipelineRunDetails {

    private List<PipelineRun> lastRuns;
    private Long lastRunTs;

    public PipelineRunDetails(List<PipelineRun> lastRuns) {
        this.lastRuns = lastRuns;
        if (!CollectionUtils.isEmpty(lastRuns)) {
            this.lastRunTs = lastRuns.get(0).getProcessedTs();
        }
    }
}
