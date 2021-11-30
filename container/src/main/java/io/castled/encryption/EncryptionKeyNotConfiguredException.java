package io.castled.encryption;


public class EncryptionKeyNotConfiguredException extends EncryptionException {

    public EncryptionKeyNotConfiguredException(Long teamId) {
        super(String.format("Encryption key not configured for team %d", teamId), null);
    }
}
