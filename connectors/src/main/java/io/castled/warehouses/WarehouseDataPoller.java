package io.castled.warehouses;

import io.castled.constants.ConnectorExecutionConstants;
import io.castled.warehouses.models.WarehousePollContext;
import io.castled.warehouses.models.WarehousePollResult;

import java.nio.file.Path;


public interface WarehouseDataPoller {

    WarehousePollResult pollRecords(WarehousePollContext warehousePollContext);

    WarehousePollResult resumePoll(WarehousePollContext warehousePollContext);

    void cleanupPipelineRunResources(WarehousePollContext warehousePollContext);

    void cleanupPipelineResources(String pipelineUUID, WarehouseConfig warehouseConfig);

    default Path getPipelineRunUnloadDirectory(String pipelineUUID, Long pipelineRunId) {
        return ConnectorExecutionConstants.WAREHOUSE_UNLOAD_DIR_PATH.resolve(pipelineUUID).resolve(String.valueOf(pipelineRunId));
    }

    default Path getPipelineUnloadDirectory(String pipelineUUID) {
        return ConnectorExecutionConstants.WAREHOUSE_UNLOAD_DIR_PATH.resolve(pipelineUUID);
    }

}
