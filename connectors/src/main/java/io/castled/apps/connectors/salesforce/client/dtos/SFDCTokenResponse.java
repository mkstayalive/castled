package io.castled.apps.connectors.salesforce.client.dtos;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SFDCTokenResponse {

    @NotNull
    private String id;
    private String accessToken;
    private String refreshToken;
    private String scope;
    private String instanceUrl;
    private String error;
    private String errorCode;
}
