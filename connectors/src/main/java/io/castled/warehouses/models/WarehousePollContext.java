package io.castled.warehouses.models;

import io.castled.models.QueryMode;
import io.castled.schema.models.RecordSchema;
import io.castled.warehouses.WarehouseConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehousePollContext {

    private WarehouseConfig warehouseConfig;
    private List<String> primaryKeys;
    private String query;

    private QueryMode queryMode;

    //uuid and not the incrementing id;
    private String pipelineUUID;

    private Long pipelineId;
    private Long pipelineRunId;

    private String dataEncryptionKey;

    private RecordSchema warehouseSchema;
}
