package io.castled.apps.connectors.marketo.dtos;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MarketoTokenResponse {

    private String id;
    @NotNull
    private String accessToken;
    private String tokenType;
    private Integer expiresIn;
    private String scope;
}
