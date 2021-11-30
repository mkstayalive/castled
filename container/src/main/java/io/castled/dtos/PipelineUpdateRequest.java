package io.castled.dtos;

import io.castled.models.jobschedule.JobSchedule;
import lombok.Data;

@Data
public class PipelineUpdateRequest {

    private String name;
    private JobSchedule schedule;
}
