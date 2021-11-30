package io.castled.commons.errors.errorclassifications;

import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.CastledErrorCode;
import io.castled.schema.SchemaUtils;
import io.castled.schema.models.Schema;

public class IncompatibleMappingError extends CastledError {

    private final String fieldName;
    private final Schema schema;

    public IncompatibleMappingError(String fieldName, Schema schema) {
        super(CastledErrorCode.INCOMPATIBLE_MAPPING);
        this.fieldName = fieldName;
        this.schema = schema;
    }

    @Override
    public String uniqueId() {
        return fieldName + "_" + SchemaUtils.getPrettyName(schema);
    }

    @Override
    public String description() {
        return String.format("Value for field %s incompatible with schema %s", fieldName, SchemaUtils.getPrettyName(schema));
    }
}
