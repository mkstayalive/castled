package io.castled.dtos;

import io.castled.warehouses.WarehouseConfig;
import io.castled.warehouses.WarehouseType;
import lombok.Data;

@Data
public class WarehouseTestConnectionRequest {

    private WarehouseConfig config;
    private WarehouseType type;
}
