package io.castled.exceptions.connect;

public class InvalidConfigException extends ConnectException {

    public InvalidConfigException(String message) {
        super(ConnectionError.INVALID_CONFIG, message);
    }
}
