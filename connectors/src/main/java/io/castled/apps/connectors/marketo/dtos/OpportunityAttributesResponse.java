package io.castled.apps.connectors.marketo.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpportunityAttributesResponse {

    private String requestId;
    private GenericAttributesWrapper result;
    private Boolean success;
}

