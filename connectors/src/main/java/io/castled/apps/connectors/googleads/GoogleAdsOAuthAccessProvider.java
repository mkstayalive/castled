package io.castled.apps.connectors.googleads;

import com.google.common.collect.Lists;
import io.castled.oauth.GoogleOAuthAccessProvider;
import io.castled.oauth.OAuthAccessConfig;
import io.castled.oauth.OAuthClientConfig;

import java.util.List;

public class GoogleAdsOAuthAccessProvider extends GoogleOAuthAccessProvider {
    public GoogleAdsOAuthAccessProvider(OAuthClientConfig oAuthClientConfig) throws Exception {
        super(oAuthClientConfig);
    }

    @Override
    public List<String> getScopes() {
        return Lists.newArrayList("https://www.googleapis.com/auth/adwords");
    }

    @Override
    public OAuthAccessConfig doGetAccessConfig(String accessToken, String refreshToken, String email) {
        return new GadsAccessConfig(email, accessToken, refreshToken);
    }
}
