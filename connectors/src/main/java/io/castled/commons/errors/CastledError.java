package io.castled.commons.errors;

import lombok.Getter;

public abstract class CastledError {

    @Getter
    private final CastledErrorCode errorCode;

    public CastledError(CastledErrorCode castledErrorCode) {
        this.errorCode = castledErrorCode;
    }

    public abstract String uniqueId();

    public abstract String description();

    public static void main(String[] args) throws Exception {

    }

}
