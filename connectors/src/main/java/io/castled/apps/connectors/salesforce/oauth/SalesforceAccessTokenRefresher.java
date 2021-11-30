package io.castled.apps.connectors.salesforce.oauth;

import com.google.inject.Inject;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.salesforce.SalesforceAccessConfig;
import io.castled.apps.connectors.salesforce.client.SFDCAuthClient;
import io.castled.apps.connectors.salesforce.client.dtos.SFDCTokenResponse;
import io.castled.cache.OAuthCache;
import io.castled.oauth.AccessTokenRefresher;
import io.castled.oauth.OAuthClientConfig;
import io.castled.oauth.OAuthDAO;
import io.castled.oauth.OAuthDetails;
import io.castled.pubsub.MessagePublisher;
import io.castled.pubsub.registry.OAuthDetailsUpdatedMessage;
import org.jdbi.v3.core.Jdbi;

public class SalesforceAccessTokenRefresher implements AccessTokenRefresher<SalesforceAccessConfig> {

    private final OAuthDAO oAuthDAO;
    private final SFDCAuthClient sfdcAuthClient;
    private final OAuthClientConfig oAuthClientConfig;
    private final OAuthCache oAuthCache;
    private final MessagePublisher messagePublisher;

    @Inject
    public SalesforceAccessTokenRefresher(OAuthClientConfig oAuthClientConfig) {
        this.oAuthDAO = ObjectRegistry.getInstance(Jdbi.class).onDemand(OAuthDAO.class);
        this.sfdcAuthClient = ObjectRegistry.getInstance(SFDCAuthClient.class);
        this.oAuthClientConfig = oAuthClientConfig;
        this.oAuthCache = ObjectRegistry.getInstance(OAuthCache.class);
        this.messagePublisher = ObjectRegistry.getInstance(MessagePublisher.class);
    }

    public SalesforceAccessConfig refreshAccessConfig(Long oAuthToken) {

        OAuthDetails oAuthDetails = this.oAuthCache.getValue(oAuthToken);
        SalesforceAccessConfig currentAccessConfig = (SalesforceAccessConfig) oAuthDetails.getAccessConfig();
        String refreshToken = oAuthDetails.getAccessConfig().getRefreshToken();
        SFDCTokenResponse sfdcTokenResponse = this.sfdcAuthClient
                .getTokenViaRefreshToken(oAuthDetails.getAccessConfig().getRefreshToken(), oAuthClientConfig.getClientId(), oAuthClientConfig.getClientSecret());
        SalesforceAccessConfig salesforceAccessConfig = SalesforceAccessConfig.builder().instanceUrl(sfdcTokenResponse.getInstanceUrl())
                .accessToken(sfdcTokenResponse.getAccessToken()).refreshToken(refreshToken).userName(currentAccessConfig.getUserName())
                .displayName(currentAccessConfig.getDisplayName()).build();

        this.oAuthDAO.updateAccessConfig(oAuthToken, salesforceAccessConfig);
        this.messagePublisher.publishMessage(new OAuthDetailsUpdatedMessage(oAuthToken));
        return salesforceAccessConfig;
    }
}
