package io.castled.apps.connectors.marketo.exception;

import io.castled.apps.connectors.marketo.dtos.ErrorResponse;
import io.castled.exceptions.CastledException;
import lombok.Getter;

public class TokenExpiredException extends CastledException {

    @Getter
    private final ErrorResponse errorResponse;

    public TokenExpiredException(ErrorResponse errorResponse) {
        super(errorResponse.getMessage());
        this.errorResponse = errorResponse;
    }
}
