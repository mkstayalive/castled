package io.castled.apps.connectors.mailchimp.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.apps.connectors.mailchimp.client.dtos.MailChimpTokenResponse;
import io.castled.apps.connectors.mailchimp.client.dtos.MailchimpMetadataResponse;
import io.castled.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Singleton
@Slf4j
public class MailchimpAuthClient {

    private final Client client;

    private static final String TOKEN_SERVICE_END_POINT = "https://login.mailchimp.com/oauth2/token";

    private static final String METADATA_END_POINT = "https://login.mailchimp.com/oauth2/metadata";

    private static final String TOKEN_REQUEST_FORM = "grant_type=authorization_code&" +
            "client_id=%s&client_secret=%s&redirect_uri=%s&code=%s";

    @Inject
    public MailchimpAuthClient(Client client) {
        this.client = client;
    }

    public String getTokenViaAuthorizationCode(String authorizationCode, String clientId,
                                               String clientSecret, String redirectUri) {
        String payload = String.format(TOKEN_REQUEST_FORM,
                clientId, clientSecret, redirectUri, authorizationCode);

        Response response = this.client.target(TOKEN_SERVICE_END_POINT)
                .request(MediaType.APPLICATION_JSON).post(Entity.entity(payload, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        return response.readEntity(MailChimpTokenResponse.class).getAccessToken();

    }

    public MailchimpMetadataResponse getAccountMetadata(String accessToken) {
        Response response = this.client.target(METADATA_END_POINT)
                .request(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .get();
        return response.readEntity(MailchimpMetadataResponse.class);
    }
}
