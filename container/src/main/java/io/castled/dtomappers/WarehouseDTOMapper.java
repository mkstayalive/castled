package io.castled.dtomappers;

import io.castled.dtos.WarehouseDTO;
import io.castled.models.Warehouse;
import io.castled.models.WarehouseAggregate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface WarehouseDTOMapper {

    WarehouseDTOMapper INSTANCE = Mappers.getMapper(WarehouseDTOMapper.class);

    WarehouseDTO toDTO(Warehouse warehouse);

    default WarehouseDTO toDTO(Warehouse warehouse, List<WarehouseAggregate> warehouseAggregates) {
        int pipelines = warehouseAggregates.stream()
                .filter(warehouseAggregate -> warehouseAggregate.getWarehouseId().equals(warehouse.getId()))
                .map(WarehouseAggregate::getPipelines).findFirst().orElse(0);
        return toDTO(warehouse, pipelines);
    }

    default WarehouseDTO toDTO(Warehouse warehouse, int pipelines) {
        WarehouseDTO warehouseDTO = toDTO(warehouse);
        warehouseDTO.setLogoUrl(warehouse.getType().getLogoUrl());
        warehouseDTO.setDocUrl(warehouse.getType().getDocUrl());
        warehouseDTO.setPipelines(pipelines);
        warehouseDTO.setAccessType(warehouse.getType().getAccessType());
        warehouseDTO.setConfig(warehouse.getConfig());
        return warehouseDTO;
    }

}
