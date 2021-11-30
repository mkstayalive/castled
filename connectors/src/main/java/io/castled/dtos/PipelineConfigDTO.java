package io.castled.dtos;

import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.models.CastledDataMapping;
import io.castled.models.QueryMode;
import io.castled.models.jobschedule.JobSchedule;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Data
public class PipelineConfigDTO {

    @NotNull
    private String name;

    @NotNull
    private JobSchedule schedule;

    @NotNull
    private Long appId;

    @NotNull
    private Long warehouseId;

    @NotNull
    private String sourceQuery;

    @NotNull
    private QueryMode queryMode;

    private AppSyncConfig appSyncConfig;

    private CastledDataMapping mapping;

}
