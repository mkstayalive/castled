package io.castled.apps.connectors.hubspot.oauth;

import io.castled.ObjectRegistry;
import io.castled.apps.connectors.hubspot.HubspotAccessConfig;
import io.castled.apps.connectors.hubspot.client.HubspotAuthClient;
import io.castled.apps.connectors.hubspot.client.dtos.HubspotTokenResponse;
import io.castled.oauth.BaseOauthAccessProvider;
import io.castled.oauth.OAuthAccessConfig;
import io.castled.oauth.OAuthClientConfig;


public class HubspotOAuthAccessProvider extends BaseOauthAccessProvider {

    private static final String AUTHORIZATION_END_POINT = "https://app.hubspot.com/oauth/authorize";
    private final HubspotAuthClient hubspotAuthClient;
    private final OAuthClientConfig oAuthClientConfig;

    public HubspotOAuthAccessProvider(OAuthClientConfig oAuthClientConfig) {
        this.hubspotAuthClient = ObjectRegistry.getInstance(HubspotAuthClient.class);
        this.oAuthClientConfig = oAuthClientConfig;
    }

    @Override
    public OAuthAccessConfig getAccessConfig(String authorizationCode, String redirectUri) {
        HubspotTokenResponse hubspotTokenResponse = this.hubspotAuthClient.getTokenViaAuthorizationCode(authorizationCode, oAuthClientConfig.getClientId(),
                oAuthClientConfig.getClientSecret(), redirectUri);
        return HubspotAccessConfig.builder()
                .accessToken(hubspotTokenResponse.getAccessToken())
                .refreshToken(hubspotTokenResponse.getRefreshToken()).build();
    }

    @Override
    public String getAuthorizationUrl(String stateId, String redirectUri, String clientId) {
        return String.format("%s?client_id=%s&redirect_uri=%s&state=%s&scope=contacts+crm.import+tickets+crm.schemas.custom.read+crm.objects.custom.read+crm.objects.custom.write",
                AUTHORIZATION_END_POINT, clientId, redirectUri, stateId);
    }
}
