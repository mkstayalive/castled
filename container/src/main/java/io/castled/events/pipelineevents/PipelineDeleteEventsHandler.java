package io.castled.events.pipelineevents;

import com.google.inject.Inject;
import io.castled.misc.PipelineScheduleManager;
import io.castled.models.Pipeline;
import io.castled.services.PipelineService;
import io.castled.models.Warehouse;
import io.castled.warehouses.WarehouseConnector;
import io.castled.warehouses.WarehouseService;
import io.castled.warehouses.WarehouseType;

import java.util.Map;

@SuppressWarnings({"rawtypes"})
public class PipelineDeleteEventsHandler implements PipelineEventsHandler {

    private final PipelineScheduleManager pipelineScheduleManager;
    private final PipelineService pipelineService;
    private final WarehouseService warehouseService;
    private final Map<WarehouseType, WarehouseConnector> warehouseConnectors;

    @Inject
    public PipelineDeleteEventsHandler(PipelineScheduleManager pipelineScheduleManager,
                                       PipelineService pipelineService, WarehouseService warehouseService,
                                       Map<WarehouseType, WarehouseConnector> warehouseConnectors) {
        this.pipelineScheduleManager = pipelineScheduleManager;
        this.pipelineService = pipelineService;
        this.warehouseConnectors = warehouseConnectors;
        this.warehouseService = warehouseService;
    }

    @Override
    public void handlePipelineEvent(PipelineEvent pipelineEvent) {
        this.pipelineScheduleManager.unschedulePipeline(pipelineEvent.getPipelineId());
        Pipeline pipeline = pipelineService.getPipeline(pipelineEvent.getPipelineId());
        Warehouse warehouse = warehouseService.getWarehouse(pipeline.getWarehouseId());
        this.warehouseConnectors.get(warehouse.getType()).getDataPoller().cleanupPipelineResources(pipeline.getUuid(), warehouse.getConfig());
    }
}
