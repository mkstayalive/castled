package io.castled.warehouses.test;

import io.castled.warehouses.WarehouseConfig;
import io.castled.warehouses.WarehouseDataPoller;
import io.castled.warehouses.models.WarehousePollContext;
import io.castled.warehouses.models.WarehousePollResult;

public class DummyWarehouseDataPoller implements WarehouseDataPoller {
    @Override
    public WarehousePollResult pollRecords(WarehousePollContext warehousePollContext) {

        DummyRecordInputStream dummyRecordInputStream = new DummyRecordInputStream();
        return WarehousePollResult.builder().recordInputStream(dummyRecordInputStream)
                .warehouseSchema(dummyRecordInputStream.getSchema()).build();
    }

    @Override
    public WarehousePollResult resumePoll(WarehousePollContext warehousePollContext) {
        return null;
    }

    @Override
    public void cleanupPipelineRunResources(WarehousePollContext warehousePollContext) {

    }

    @Override
    public void cleanupPipelineResources(String pipelineUUID, WarehouseConfig warehouseConfig) {

    }
}
