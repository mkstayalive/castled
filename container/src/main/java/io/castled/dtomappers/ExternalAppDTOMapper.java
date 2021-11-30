package io.castled.dtomappers;

import io.castled.apps.ExternalApp;
import io.castled.dtos.ExternalAppDTO;
import io.castled.models.AppAggregate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ExternalAppDTOMapper {

    ExternalAppDTOMapper INSTANCE = Mappers.getMapper(ExternalAppDTOMapper.class);

    ExternalAppDTO toDTO(ExternalApp externalApp);

    default ExternalAppDTO toDTO(ExternalApp externalApp, int pipelines) {
        ExternalAppDTO externalAppDTO = toDTO(externalApp);
        externalAppDTO.setLogoUrl(externalApp.getType().logoUrl());
        externalAppDTO.setPipelines(pipelines);
        externalAppDTO.setAccessType(externalApp.getType().getAccessType());
        externalAppDTO.setConfig(externalApp.getConfig());
        externalAppDTO.setDocUrl(externalApp.getType().docUrl());
        return externalAppDTO;
    }

    default ExternalAppDTO toDTO(ExternalApp externalApp, List<AppAggregate> appAggregates) {
        int pipelines = appAggregates.stream()
                .filter(appAggregate -> appAggregate.getAppId().equals(externalApp.getId()))
                .map(AppAggregate::getPipelines).findFirst().orElse(0);
        return toDTO(externalApp, pipelines);
    }
}
