package io.castled.apps.dtos;

import io.castled.apps.syncconfigs.AppSyncConfig;
import lombok.Data;

@Data
public class AppSyncConfigDTO {
    private String name;
    private Long appId;
    private Long warehouseId;
    private String sourceQuery;

    private AppSyncConfig appSyncConfig;
}
