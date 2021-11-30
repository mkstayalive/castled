package io.castled.apps.connectors.sendgrid.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ContactAttribute {

    private String id;
    private String name;
    private String fieldType;
    private Boolean readOnly;

    public static final String EMAIL = "email";
    public static final String CUSTOM_FIELDS = "custom_fields";
}
