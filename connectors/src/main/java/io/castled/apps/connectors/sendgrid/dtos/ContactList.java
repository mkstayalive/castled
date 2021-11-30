package io.castled.apps.connectors.sendgrid.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class ContactList {

    private String id;
    private String name;
    private Integer contactCount;
    @JsonProperty("_metadata")
    private ContactListMetadata metadata;
}
