package io.castled.warehouses;

import com.google.inject.Inject;
import io.castled.constants.CommonConstants;
import io.castled.jarvis.taskmanager.TaskExecutor;
import io.castled.jarvis.taskmanager.models.Task;
import io.castled.models.QueryResults;
import io.castled.models.Warehouse;
import io.castled.utils.JsonUtils;

import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class QueryPreviewExecutor implements TaskExecutor {

    private final Map<WarehouseType, WarehouseConnector> warehouseConnectors;
    private final WarehouseService warehouseService;

    @Inject
    public QueryPreviewExecutor(Map<WarehouseType, WarehouseConnector> warehouseConnectors,
                                WarehouseService warehouseService) {
        this.warehouseConnectors = warehouseConnectors;
        this.warehouseService = warehouseService;
    }

    @Override
    public String executeTask(Task task) throws Exception {
        Long warehouseId = ((Number) task.getParams().get(CommonConstants.WAREHOUSE_ID)).longValue();
        String query = (String) task.getParams().get(CommonConstants.QUERY);
        Warehouse warehouse = this.warehouseService.getWarehouse(warehouseId);
        QueryResults queryResults = warehouseConnectors.get(warehouse.getType()).previewQuery(query, warehouse.getConfig(), 25);
        return JsonUtils.objectToString(queryResults);
    }
}
