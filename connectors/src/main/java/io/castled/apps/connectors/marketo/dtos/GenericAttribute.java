package io.castled.apps.connectors.marketo.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericAttribute {

    private String name;
    private String displayName;
    private String dataType;
    private Integer length;
    private Boolean updateable;
    private RestAttrs rest;
}
