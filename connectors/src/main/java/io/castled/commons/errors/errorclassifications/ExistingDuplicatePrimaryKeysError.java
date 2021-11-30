package io.castled.commons.errors.errorclassifications;


import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.CastledErrorCode;

public class ExistingDuplicatePrimaryKeysError extends CastledError {

    private final String errorMessage;

    public ExistingDuplicatePrimaryKeysError(String errorMessage) {
        super(CastledErrorCode.EXISTING_DUPLICATE_PRIMARY_KEYS);
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
