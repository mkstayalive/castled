package io.castled.apps.connectors.hubspot.client;

import com.google.inject.Inject;
import io.castled.apps.connectors.hubspot.client.dtos.HubspotTokenResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class HubspotAuthClient {

    private static final String TOKEN_SERVICE_END_POINT = "https://api.hubapi.com/oauth/v1/token";

    private final Client client;

    @Inject
    public HubspotAuthClient(Client client) {
        this.client = client;
    }

    public HubspotTokenResponse getTokenViaAuthorizationCode(String authorizationCode, String clientId,
                                                             String clientSecret, String redirectUri) {
        Response response = this.client.target(TOKEN_SERVICE_END_POINT)
                .queryParam("code", authorizationCode)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("redirect_uri", redirectUri)
                .request(MediaType.APPLICATION_JSON).post(Entity.entity(null, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        return response.readEntity(HubspotTokenResponse.class);
    }

    public HubspotTokenResponse getTokenViaRefreshToken(String refreshToken, String clientId,
                                                             String clientSecret) {
        Response response = this.client.target(TOKEN_SERVICE_END_POINT)
                .queryParam("refresh_token", refreshToken)
                .queryParam("grant_type", "refresh_token")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .request(MediaType.APPLICATION_JSON).post(Entity.entity(null, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        return response.readEntity(HubspotTokenResponse.class);
    }

}
