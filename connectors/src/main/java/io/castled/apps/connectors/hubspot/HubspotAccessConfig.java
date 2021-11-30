package io.castled.apps.connectors.hubspot;

import io.castled.oauth.OAuthAccessConfig;
import io.castled.oauth.OAuthServiceType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HubspotAccessConfig extends OAuthAccessConfig {

    @Builder
    public HubspotAccessConfig(String accessToken, String refreshToken) {
        super(OAuthServiceType.HUBSPOT, accessToken, refreshToken);
    }

}
