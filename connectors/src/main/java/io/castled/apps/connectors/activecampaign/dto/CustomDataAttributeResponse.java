package io.castled.apps.connectors.activecampaign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomDataAttributeResponse {

    private List<CustomDataAttribute> fields;
}
