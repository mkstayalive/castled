package io.castled.apps.connectors.marketo.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CompanyAttribute {

    private String name;
    private String displayName;
    private String dataType;
    private Integer length;
    private Boolean updateable;
    private Boolean crmManaged;
}
