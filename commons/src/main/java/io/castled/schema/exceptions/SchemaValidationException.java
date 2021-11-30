package io.castled.schema.exceptions;


import io.castled.schema.models.SchemaType;
import lombok.Getter;

public class SchemaValidationException extends SchemaException {

    @Getter
    private final SchemaType schemaType;

    public SchemaValidationException(SchemaType schemaType, String message) {
        super(message);
        this.schemaType = schemaType;
    }
}
