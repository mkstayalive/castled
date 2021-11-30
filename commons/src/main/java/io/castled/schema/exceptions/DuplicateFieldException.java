package io.castled.schema.exceptions;

public class DuplicateFieldException extends SchemaException {

    public DuplicateFieldException(String fieldName) {
        super(String.format("Field %s exists on the record schema", fieldName));
    }
}
