package io.castled.models;
import io.castled.warehouses.WarehouseConfig;
import io.castled.warehouses.WarehouseStatus;
import io.castled.warehouses.WarehouseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Warehouse {
    private Long id;
    private String name;
    private Long teamId;
    private WarehouseConfig config;
    private WarehouseStatus status;
    private WarehouseType type;
}