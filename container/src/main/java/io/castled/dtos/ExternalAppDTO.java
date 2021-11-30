package io.castled.dtos;

import io.castled.apps.AppConfig;
import io.castled.apps.ExternalAppStatus;
import io.castled.apps.ExternalAppType;
import io.castled.commons.models.AccessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExternalAppDTO {
    private Long id;
    private String name;
    private Long teamId;
    private AppConfig config;
    private ExternalAppStatus status;
    private ExternalAppType type;
    private int pipelines;
    private String logoUrl;
    private String docUrl;
    private AccessType accessType;
}
