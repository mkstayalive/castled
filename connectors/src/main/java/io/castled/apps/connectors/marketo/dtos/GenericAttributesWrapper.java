package io.castled.apps.connectors.marketo.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericAttributesWrapper {

    private String name;
    private String description;
    private String idField;
    private List<String> dedupeFields;
    private List<List<String>> searchableFields;
    private List<GenericAttribute> fields;

    public static final String ID_FIELD = "idField";
    public static final String DEDUPE_FIELD = "dedupeFields";
}
