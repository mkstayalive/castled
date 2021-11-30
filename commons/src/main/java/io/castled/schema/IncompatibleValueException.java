package io.castled.schema;

import io.castled.exceptions.CastledException;
import io.castled.schema.models.Schema;

public class IncompatibleValueException extends CastledException {

    private final Object value;
    private final Schema schema;

    public IncompatibleValueException(Object value, Schema schema) {
        super(String.format("Value %s incompatible with schema %s", value, schema.getType()));
        this.value = value;
        this.schema = schema;
    }
}
