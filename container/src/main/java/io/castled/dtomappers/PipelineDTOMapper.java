package io.castled.dtomappers;

import io.castled.ObjectRegistry;
import io.castled.apps.ExternalApp;
import io.castled.apps.ExternalAppService;
import io.castled.dtos.AppDetails;
import io.castled.dtos.PipelineDTO;
import io.castled.dtos.PipelineRunDetails;
import io.castled.dtos.WarehouseDetails;
import io.castled.models.Pipeline;
import io.castled.services.PipelineService;
import io.castled.models.Warehouse;
import io.castled.warehouses.WarehouseService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PipelineDTOMapper {
    PipelineDTOMapper INSTANCE = Mappers.getMapper(PipelineDTOMapper.class);

    @Mapping(target = "app", source = "appId", qualifiedByName = "getAppDetails")
    @Mapping(target = "warehouse", source = "warehouseId", qualifiedByName = "getWarehouseDetails")
    @Mapping(target = "lastRunDetails", source = "pipeline", qualifiedByName = "getLastRunDetails")
    PipelineDTO toDetailedDTO(Pipeline pipeline);

    @Mapping(target = "app", source = "appId", qualifiedByName = "getAppDetails")
    @Mapping(target = "warehouse", source = "warehouseId", qualifiedByName = "getWarehouseDetails")
    PipelineDTO toDTO(Pipeline pipeline);

    default AppDetails getAppDetails(Long appId) {
        ExternalApp externalApp = ObjectRegistry.getInstance(ExternalAppService.class).getExternalApp(appId, true);
        return AppDetails.builder().name(externalApp.getName())
                .id(externalApp.getId()).type(externalApp.getType()).build();
    }

    default WarehouseDetails getWarehouseDetails(Long warehouseId) {
        Warehouse warehouse = ObjectRegistry.getInstance(WarehouseService.class).getWarehouse(warehouseId, true);
        return WarehouseDetails.builder().id(warehouse.getId()).name(warehouse.getName()).type(warehouse.getType()).build();
    }

    default PipelineRunDetails getLastRunDetails(Pipeline pipeline) {
        return new PipelineRunDetails(ObjectRegistry.getInstance(PipelineService.class).getPipelineRuns(pipeline.getId(), 10));

    }
}
