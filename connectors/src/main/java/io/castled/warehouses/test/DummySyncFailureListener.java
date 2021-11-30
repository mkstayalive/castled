package io.castled.warehouses.test;

import io.castled.schema.models.Tuple;
import io.castled.warehouses.WarehouseConfig;
import io.castled.warehouses.WarehouseSyncFailureListener;
import io.castled.warehouses.models.WarehousePollContext;


public class DummySyncFailureListener extends WarehouseSyncFailureListener {

    public DummySyncFailureListener(WarehousePollContext warehousePollContext) {
        super(warehousePollContext);
    }


    @Override
    public void cleanupResources(String pipelineUUID, Long pipelineRunId, WarehouseConfig warehouseConfig) {

    }

    @Override
    public void doFlush() throws Exception {
        System.out.println("Inside failure listener: flush");

    }

    @Override
    public void doWriteRecord(Tuple record) throws Exception {
        System.out.println("Inside failure listener: write record");
    }
}
