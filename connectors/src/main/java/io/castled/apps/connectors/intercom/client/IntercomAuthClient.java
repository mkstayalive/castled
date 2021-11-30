package io.castled.apps.connectors.intercom.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.apps.connectors.intercom.client.dtos.AccessTokenResponse;
import io.castled.utils.RestUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Singleton
public class IntercomAuthClient {

    private static final String TOKEN_SERVICE_END_POINT = "https://api.intercom.io/auth/eagle/token";
    private static final String USER_INFO_END_POINT = "https://api.intercom.io/me";

    private final Client client;

    @Inject
    public IntercomAuthClient(Client client) {
        this.client = client;
    }

    public String getAccessToken(String authorizationCode, String clientId,
                                 String clientSecret) {
        Response response = this.client.target(TOKEN_SERVICE_END_POINT)
                .queryParam("code", authorizationCode)
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(null));

        AccessTokenResponse accessTokenResponse = response.readEntity(AccessTokenResponse.class);
        return accessTokenResponse.getAccessToken();
    }

    public void getUserInfo(String accessToken) {

        Response response = this.client.target(USER_INFO_END_POINT)
                .request(MediaType.APPLICATION_JSON).header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .get();
        System.out.println(response);


    }
}
