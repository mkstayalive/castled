package io.castled.schema.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class FieldSchema {
    private String name;
    private Schema schema;
    private Map<String, Object> params;

    public FieldSchema(String name, Schema schema) {
        this.name = name;
        this.schema = schema;
    }
}
