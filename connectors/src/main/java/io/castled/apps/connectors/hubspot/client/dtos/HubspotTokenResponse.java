package io.castled.apps.connectors.hubspot.client.dtos;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class HubspotTokenResponse {
    private String refreshToken;
    private String accessToken;
    private int expiresIn;
}
