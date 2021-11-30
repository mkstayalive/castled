package io.castled.apps.connectors.intercom.client.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataAttributesResponse {

    private List<DataAttribute> data;
}
