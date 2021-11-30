package io.castled.oauth;

import io.castled.ObjectRegistry;
import org.jdbi.v3.core.Jdbi;

public abstract class BaseOauthAccessProvider implements OAuthAccessProvider {

    public Long persistAccessConfig(String authorizationCode, String redirectUri) {
        OAuthAccessConfig accessConfig = getAccessConfig(authorizationCode, redirectUri);
        OAuthDAO oAuthDAO = ObjectRegistry.getInstance(Jdbi.class).onDemand(OAuthDAO.class);
        OAuthDetails oAuthDetails = OAuthDetails.builder().accessConfig(accessConfig).build();
        return oAuthDAO.createOAuthDetails(oAuthDetails);
    }

    public abstract OAuthAccessConfig getAccessConfig(String authorizationCode, String redirectUri);

}
