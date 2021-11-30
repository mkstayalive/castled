package io.castled.apps.connectors.hubspot.oauth;

import io.castled.ObjectRegistry;
import io.castled.apps.connectors.hubspot.HubspotAccessConfig;
import io.castled.apps.connectors.hubspot.client.HubspotAuthClient;
import io.castled.apps.connectors.hubspot.client.dtos.HubspotTokenResponse;
import io.castled.cache.OAuthCache;
import io.castled.oauth.AccessTokenRefresher;
import io.castled.oauth.OAuthClientConfig;
import io.castled.oauth.OAuthDAO;
import io.castled.oauth.OAuthDetails;
import io.castled.pubsub.MessagePublisher;
import io.castled.pubsub.registry.OAuthDetailsUpdatedMessage;
import org.jdbi.v3.core.Jdbi;

public class HubspotAccessTokenRefresher implements AccessTokenRefresher<HubspotAccessConfig> {

    private final OAuthDAO oAuthDAO;
    private final HubspotAuthClient hubspotAuthClient;
    private final OAuthClientConfig oAuthClientConfig;
    private final OAuthCache oAuthCache;
    private final MessagePublisher messagePublisher;

    public HubspotAccessTokenRefresher(OAuthClientConfig oAuthClientConfig) {
        this.oAuthDAO = ObjectRegistry.getInstance(Jdbi.class).onDemand(OAuthDAO.class);
        this.hubspotAuthClient = ObjectRegistry.getInstance(HubspotAuthClient.class);
        this.oAuthClientConfig = oAuthClientConfig;
        this.oAuthCache = ObjectRegistry.getInstance(OAuthCache.class);
        this.messagePublisher = ObjectRegistry.getInstance(MessagePublisher.class);
    }

    public HubspotAccessConfig refreshAccessConfig(Long oAuthToken) {

        OAuthDetails oAuthDetails = this.oAuthCache.getValue(oAuthToken);
        HubspotTokenResponse hubspotTokenResponse = this.hubspotAuthClient
                .getTokenViaRefreshToken(oAuthDetails.getAccessConfig().getRefreshToken(), oAuthClientConfig.getClientId(), oAuthClientConfig.getClientSecret());
        HubspotAccessConfig hubspotAccessConfig = HubspotAccessConfig.builder().accessToken(hubspotTokenResponse.getAccessToken())
                .refreshToken(hubspotTokenResponse.getRefreshToken()).build();

        this.oAuthDAO.updateAccessConfig(oAuthToken, hubspotAccessConfig);
        this.messagePublisher.publishMessage(new OAuthDetailsUpdatedMessage(oAuthToken));
        return hubspotAccessConfig;
    }
}
