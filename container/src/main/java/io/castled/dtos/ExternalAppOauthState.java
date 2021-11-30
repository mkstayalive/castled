package io.castled.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalAppOauthState {
    private Long userId;
    private Long appId;
    private OAuthAppAttributes oAuthAppAttributes;
}
