package io.castled.commons.errors.errorclassifications;

import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.CastledErrorCode;
import lombok.Getter;

@Getter
public class InvalidFieldValueError extends CastledError {

    private final String fieldName;
    private final String errorType;
    private final String description;

    public InvalidFieldValueError(String fieldName, String errorType, String description) {
        super(CastledErrorCode.INVALID_FIELD_VALUE);
        this.fieldName = fieldName;
        this.errorType = errorType;
        this.description = description;
    }

    @Override
    public String uniqueId() {
        return fieldName + "_" + errorType;
    }

    @Override
    public String description() {
        return description;
    }
}
