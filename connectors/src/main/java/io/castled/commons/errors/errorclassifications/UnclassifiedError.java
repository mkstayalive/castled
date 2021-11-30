package io.castled.commons.errors.errorclassifications;


import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.CastledErrorCode;

import java.util.Optional;

public class UnclassifiedError extends CastledError {

    private final String errorMessage;

    public UnclassifiedError(String errorMessage) {
        super(CastledErrorCode.UNCLASSIFIED_ERROR);
        this.errorMessage = errorMessage;
    }

    @Override
    public String uniqueId() {
        return "42";
    }

    @Override
    public String description() {
        return Optional.ofNullable(errorMessage).orElse("Unknown Error");
    }
}
