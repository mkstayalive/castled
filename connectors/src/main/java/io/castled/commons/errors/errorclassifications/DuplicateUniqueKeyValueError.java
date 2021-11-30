package io.castled.commons.errors.errorclassifications;

import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.CastledErrorCode;

public class DuplicateUniqueKeyValueError extends CastledError {

    private final String error;

    public DuplicateUniqueKeyValueError(String error) {
        super(CastledErrorCode.DUPLICATE_UNIQUE_KEY_VALUES);
        this.error = error;
    }

    @Override
    public String uniqueId() {
        return "42";
    }

    @Override
    public String description() {
        return error;
    }
}
