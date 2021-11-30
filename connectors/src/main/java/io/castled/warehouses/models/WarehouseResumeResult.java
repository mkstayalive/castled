package io.castled.warehouses.models;

import io.castled.commons.streams.RecordInputStream;
import io.castled.schema.models.RecordSchema;

public class WarehouseResumeResult {
    private RecordInputStream recordInputStream;
    private RecordSchema warehouseSchema;
    private boolean resumed;
}
