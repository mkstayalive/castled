package io.castled.apps.connectors.marketo.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeadAttributesResponse {

    private String requestId;
    private List<GenericAttribute> result;
    private Boolean success;
}