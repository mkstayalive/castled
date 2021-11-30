package io.castled.dtos;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.castled.commons.models.AccessType;
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
public class WarehouseDTO {
    private Long id;
    private String name;
    private Long teamId;
    private WarehouseConfig config;
    private WarehouseStatus status;
    private WarehouseType type;
    private int pipelines;
    private String logoUrl;
    private String docUrl;
    private AccessType accessType;
}