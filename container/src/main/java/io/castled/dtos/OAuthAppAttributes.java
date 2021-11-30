package io.castled.dtos;

import io.castled.apps.OAuthAppConfig;
import lombok.Data;

@Data
public class OAuthAppAttributes {
    private String name;
    private OAuthAppConfig config;
    private String successUrl;
    private String failureUrl;
    private String serverUrl;
}
