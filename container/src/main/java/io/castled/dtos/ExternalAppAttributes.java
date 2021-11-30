package io.castled.dtos;

import io.castled.apps.AppConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalAppAttributes {
    private String name;
    private AppConfig config;
}
