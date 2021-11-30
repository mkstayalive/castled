package io.castled.apps.connectors.googleads;

import io.castled.oauth.OAuthAccessConfig;
import io.castled.oauth.OAuthServiceType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GadsAccessConfig extends OAuthAccessConfig {
    private String email;

    @Builder
    public GadsAccessConfig(String email, String accessToken, String refreshToken) {
        super(OAuthServiceType.GADS, accessToken, refreshToken);
        this.email = email;
    }
}
