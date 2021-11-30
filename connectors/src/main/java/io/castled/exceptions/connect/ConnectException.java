package io.castled.exceptions.connect;

import io.castled.exceptions.CastledException;
import lombok.Getter;

public class ConnectException extends CastledException {

    @Getter
    private ConnectionError connectionError;

    public ConnectException(ConnectionError connectionError, String errorMessage) {
        super(errorMessage);
        this.connectionError = connectionError;

    }

}
