package io.castled.encryption;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtils {

    public static SecretKey generateKey(int size) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(size);
        return keyGenerator.generateKey();
    }

    public static String generateEncryptionKey(int size) {
        SecureRandom random = new SecureRandom();
        byte[] encryptionKey = new byte[size];
        random.nextBytes(encryptionKey);
        return Base64.getEncoder().encodeToString(encryptionKey);
    }

}
