package io.castled.schema.exceptions;

import io.castled.schema.models.SchemaType;

public class NullValueException extends SchemaValidationException {

    public NullValueException(SchemaType schemaType) {
        super(schemaType, "Value absent for optional field");
    }
}
