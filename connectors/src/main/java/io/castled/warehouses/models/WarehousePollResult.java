package io.castled.warehouses.models;

import io.castled.commons.streams.RecordInputStream;
import io.castled.schema.models.RecordSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehousePollResult {
    private RecordInputStream recordInputStream;
    private RecordSchema warehouseSchema;
    private boolean resumed;
}
