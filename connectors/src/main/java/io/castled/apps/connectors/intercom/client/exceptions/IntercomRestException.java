package io.castled.apps.connectors.intercom.client.exceptions;

import io.castled.apps.connectors.intercom.client.dtos.IntercomErrorResponse;
import io.castled.exceptions.CastledException;
import lombok.Getter;

public class IntercomRestException extends CastledException {
    @Getter
    private final IntercomErrorResponse errorResponse;

    public IntercomRestException(IntercomErrorResponse errorResponse) {
        super(errorResponse.getErrors().get(0).getMessage());
        this.errorResponse = errorResponse;
    }

}
