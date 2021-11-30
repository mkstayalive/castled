package io.castled.dtos;


import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.models.CastledDataMapping;
import io.castled.models.QueryMode;
import io.castled.models.PipelineStatus;
import io.castled.models.PipelineSyncStatus;
import io.castled.models.jobschedule.JobSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PipelineDTO {
    private Long id;
    private Long seqId;
    private Long teamId;
    private String uuid;
    private String name;

    private JobSchedule jobSchedule;

    private String sourceQuery;
    private PipelineStatus status;
    private PipelineSyncStatus syncStatus;

    private AppSyncConfig appSyncConfig;

    private CastledDataMapping dataMapping;

    private AppDetails app;

    private QueryMode queryMode;

    private WarehouseDetails warehouse;
    private boolean isDeleted;
    private PipelineRunDetails lastRunDetails;
}


