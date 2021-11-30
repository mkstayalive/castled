package io.castled.apps.connectors.marketo.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeadAttribute {

    private Integer id;
    private String displayName;
    private String dataType;
    private Integer length;
    private RestAttrs rest;

    public static final String ID = "id";
}
