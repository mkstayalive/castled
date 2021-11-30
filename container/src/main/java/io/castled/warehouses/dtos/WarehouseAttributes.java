package io.castled.warehouses.dtos;

import io.castled.warehouses.WarehouseConfig;
import io.castled.warehouses.WarehouseType;
import lombok.Data;

@Data
public class WarehouseAttributes {
    private String name;
    private WarehouseConfig config;
}
