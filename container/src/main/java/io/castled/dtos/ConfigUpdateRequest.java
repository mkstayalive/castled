package io.castled.dtos;

import io.castled.warehouses.WarehouseConfig;
import lombok.Data;

@Data
public class ConfigUpdateRequest {

    private WarehouseConfig config;
}
