package io.castled.apps.connectors.salesforce.oauth;

import io.castled.ObjectRegistry;
import io.castled.apps.connectors.salesforce.SalesforceAccessConfig;
import io.castled.apps.connectors.salesforce.client.SFDCAuthClient;
import io.castled.apps.connectors.salesforce.client.dtos.SFDCIdResponse;
import io.castled.apps.connectors.salesforce.client.dtos.SFDCTokenResponse;
import io.castled.oauth.BaseOauthAccessProvider;
import io.castled.oauth.OAuthAccessConfig;
import io.castled.oauth.OAuthClientConfig;
import io.castled.oauth.RefreshableOAuthTokenAccessProvider;


public class SalesforceOAuthAccessProvider extends BaseOauthAccessProvider implements RefreshableOAuthTokenAccessProvider {

    private static final String AUTHORIZATION_END_POINT = "https://login.salesforce.com/services/oauth2/authorize";
    private final SFDCAuthClient sfdcAuthClient;
    private final OAuthClientConfig oAuthClientConfig;

    public SalesforceOAuthAccessProvider(OAuthClientConfig oAuthClientConfig) {
        this.sfdcAuthClient = ObjectRegistry.getInstance(SFDCAuthClient.class);
        this.oAuthClientConfig = oAuthClientConfig;
    }

    @Override
    public String getAuthorizationUrl(String stateId, String redirectUri, String clientId) {
        return String.format("%s?response_type=code&client_id=%s&redirect_uri=%s&state=%s&prompt=login&scope=refresh_token+id+api",
                AUTHORIZATION_END_POINT, clientId, redirectUri, stateId);
    }

    @Override
    public OAuthAccessConfig getAccessConfig(String authorizationCode, String redirectUri) {
        SFDCTokenResponse sfdcTokenResponse = this.sfdcAuthClient.getTokenViaAuthorizationCode(authorizationCode, oAuthClientConfig.getClientId(),
                oAuthClientConfig.getClientSecret(), redirectUri);
        SFDCIdResponse sfdcIdResponse = this.sfdcAuthClient.getSFDCIdResponse(sfdcTokenResponse.getId(), sfdcTokenResponse.getAccessToken());
        return SalesforceAccessConfig.builder()
                .accessToken(sfdcTokenResponse.getAccessToken()).refreshToken(sfdcTokenResponse.getRefreshToken())
                .instanceUrl(sfdcTokenResponse.getInstanceUrl()).displayName(sfdcIdResponse.getDisplayName())
                .userName(sfdcIdResponse.getUsername()).build();
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        return this.sfdcAuthClient.getTokenViaRefreshToken(refreshToken,
                oAuthClientConfig.getClientId(), oAuthClientConfig.getClientSecret()).getAccessToken();

    }
}
