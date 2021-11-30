package io.castled.schema.models;

import io.castled.schema.exceptions.SchemaValidationException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class Field {

    private String name;
    private Schema schema;
    private Object value;
    private Map<String, Object> params;

    public Field(FieldSchema fieldSchema, Object value) throws SchemaValidationException {
        this.schema = fieldSchema.getSchema();
        this.name = fieldSchema.getName();
        this.value = value;
        this.params = fieldSchema.getParams();
        schema.validateValue(value);
    }
}
