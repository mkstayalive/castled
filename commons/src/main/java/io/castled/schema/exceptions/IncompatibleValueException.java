package io.castled.schema.exceptions;

import io.castled.schema.models.SchemaType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncompatibleValueException extends SchemaValidationException {

    private SchemaType schemaType;
    private Object value;

    public IncompatibleValueException(SchemaType schemaType, Object value) {
        super(schemaType, String.format("Value %s incompatible with schema %s", value, schemaType));
    }
}
