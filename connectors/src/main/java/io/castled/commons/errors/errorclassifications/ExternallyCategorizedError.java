package io.castled.commons.errors.errorclassifications;

import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.CastledErrorCode;

public class ExternallyCategorizedError extends CastledError {

    private final String errorCode;
    private final String description;

    public ExternallyCategorizedError(String errorCode, String description) {
        super(CastledErrorCode.EXTERNAL_CATEGORIZED_ERROR);
        this.errorCode = errorCode;
        this.description = description;
    }

    @Override
    public String uniqueId() {
        return errorCode;
    }

    @Override
    public String description() {
        return description;
    }
}
