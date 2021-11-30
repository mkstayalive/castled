package io.castled.apps.connectors.activecampaign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomDataAttribute {

    private Integer id ;
    private String title;
    private String descript;
    private String type;
    private String visible;
    private String orderNumber;
    private String isrequired;
    private String perstag;
}
