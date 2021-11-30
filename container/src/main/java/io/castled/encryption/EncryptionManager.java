package io.castled.encryption;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.cache.CastledCache;
import io.castled.daos.EncryptionKeysDAO;
import io.castled.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Singleton
public class EncryptionManager {
    public static final String AES_ALGORITHM = "AES";

    private final CastledCache<Long, String> teamEncryptionKeys;

    @Inject
    public EncryptionManager(Jdbi jdbi) {
        EncryptionKeysDAO encryptionKeysDAO = jdbi.onDemand(EncryptionKeysDAO.class);
        this.teamEncryptionKeys = new CastledCache<>(TimeUtils.hoursToMillis(3),
                1000, encryptionKeysDAO::getEncryptionKey, false);
    }

    public String getEncryptionKey(Long teamId) {
        return this.teamEncryptionKeys.getValue(teamId);
    }

    public String encryptText(String plainText, long teamId) throws EncryptionException {

        try {
            String encryptionKey = this.teamEncryptionKeys.getValue(teamId);
            if (encryptionKey == null) {
                throw new EncryptionKeyNotConfiguredException(teamId);
            }
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(Base64.getDecoder().decode(encryptionKey), AES_ALGORITHM));
            byte[] cipherText = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            log.error("Encrypt text failed for {} and team {}", plainText, teamId, e);
            throw new EncryptionException(Optional.ofNullable(e.getMessage()).orElse("Unknown Error"), e);
        }
    }

    public String decryptText(String cipherText, Long teamId) throws EncryptionException {
        try {
            String encryptionKey = this.teamEncryptionKeys.getValue(teamId);
            if (encryptionKey == null) {
                throw new EncryptionKeyNotConfiguredException(teamId);
            }
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new
                    SecretKeySpec(Base64.getDecoder().decode(encryptionKey), AES_ALGORITHM));
            byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(plainText);
        } catch (Exception e) {
            log.error("Decrypt text failed for {} and team {}", cipherText, teamId);
            throw new EncryptionException(Optional.ofNullable(e.getMessage()).orElse("Unknown Error"), e);
        }
    }
}
