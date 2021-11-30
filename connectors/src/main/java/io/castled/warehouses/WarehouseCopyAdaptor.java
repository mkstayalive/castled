package io.castled.warehouses;


import io.castled.schema.models.Schema;

public interface WarehouseCopyAdaptor {

    Object constructSyncableRecord(Object value, Schema schema);
}
