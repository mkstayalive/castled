package io.castled.apps.connectors.sendgrid.dtos;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class ContactListsResponse {

    List<ContactList> result;
    @JsonProperty("_metadata")
    ContactListMetadata  metadata;
}
