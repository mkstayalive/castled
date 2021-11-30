package io.castled.apps.connectors.mailchimp;

import com.google.inject.Inject;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.mailchimp.client.MailchimpAuthClient;
import io.castled.apps.connectors.mailchimp.client.dtos.MailchimpMetadataResponse;
import io.castled.oauth.BaseOauthAccessProvider;
import io.castled.oauth.OAuthAccessConfig;
import io.castled.oauth.OAuthClientConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MailchimpOAuthAccessProvider extends BaseOauthAccessProvider {

    private static final String AUTHORIZATION_END_POINT = "https://login.mailchimp.com/oauth2/authorize";
    private final MailchimpAuthClient mailchimpAuthClient;
    private final OAuthClientConfig oAuthClientConfig;

    @Inject
    public MailchimpOAuthAccessProvider(OAuthClientConfig oAuthClientConfig) {
        this.mailchimpAuthClient = ObjectRegistry.getInstance(MailchimpAuthClient.class);
        this.oAuthClientConfig = oAuthClientConfig;
    }

    @Override
    public String getAuthorizationUrl(String stateId, String redirectUri, String clientId) {
        return String.format("%s?response_type=code&client_id=%s&redirect_uri=%s&state=%s",
                AUTHORIZATION_END_POINT, clientId, redirectUri, stateId);
    }

    @Override
    public OAuthAccessConfig getAccessConfig(String authorizationCode, String redirectUri) {

        String accessToken = this.mailchimpAuthClient.getTokenViaAuthorizationCode(authorizationCode,
                this.oAuthClientConfig.getClientId(), this.oAuthClientConfig.getClientSecret(), redirectUri);

        MailchimpMetadataResponse metadata = this.mailchimpAuthClient.getAccountMetadata(accessToken);

        return MailchimpAccessConfig.builder().accessToken(accessToken).datacenter(metadata.getDc())
                .loginEmail(metadata.getLoginEmail()).apiEndPoint(metadata.getApiEndpoint()).build();
    }
}
