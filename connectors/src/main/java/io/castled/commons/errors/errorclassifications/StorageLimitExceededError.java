package io.castled.commons.errors.errorclassifications;


import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.CastledErrorCode;

public class StorageLimitExceededError extends CastledError {

    public StorageLimitExceededError() {
        super(CastledErrorCode.STORAGE_LIMIT_EXCEEDED);
    }

    @Override
    public String uniqueId() {
        return "42";
    }

    @Override
    public String description() {
        return "Storage limit exceeded";
    }
}
