package io.castled.commons.errors.errorclassifications;


import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.CastledErrorCode;

public class DuplicatePrimaryKeyValueError extends CastledError {

    private final String errorMessage;

    public DuplicatePrimaryKeyValueError(String errorMessage) {
        super(CastledErrorCode.DUPLICATE_PRIMARY_KEY_VALUES);
        this.errorMessage = errorMessage;
    }

    @Override
    public String uniqueId() {
        return "42";
    }

    @Override
    public String description() {
        return errorMessage;
    }
}
