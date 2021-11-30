package io.castled.models;

import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.models.jobschedule.JobSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Pipeline {

    private Long id;
    private Long seqId;
    private Long teamId;
    private String uuid;
    private String name;

    private QueryMode queryMode;

    private JobSchedule jobSchedule;

    private AppSyncConfig appSyncConfig;

    private CastledDataMapping dataMapping;

    private String sourceQuery;
    private PipelineStatus status;
    private PipelineSyncStatus syncStatus;

    private Long appId;
    private Long warehouseId;
    private boolean isDeleted;

}
