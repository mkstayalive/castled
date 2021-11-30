package io.castled.apps.connectors.hubspot.client.exception;

import io.castled.apps.connectors.hubspot.client.dtos.BatchObjectError;
import io.castled.exceptions.CastledException;
import lombok.Getter;

public class BatchObjectException extends CastledException {

    @Getter
    private final BatchObjectError batchObjectError;

    public BatchObjectException(BatchObjectError batchObjectError) {
        super(batchObjectError.getMessage());
        this.batchObjectError = batchObjectError;
    }
}
