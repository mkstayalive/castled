package io.castled.encryption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamEncryptionKey {
    private Long id;
    private Long teamId;
    private String cipherKey;

}
