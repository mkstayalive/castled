package io.castled.apps.connectors.salesforce.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.apps.connectors.salesforce.client.dtos.SFDCIdResponse;
import io.castled.apps.connectors.salesforce.client.dtos.SFDCTokenResponse;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.utils.RestUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Singleton
public class SFDCAuthClient {

    private static final String TOKEN_SERVICE_END_POINT = "https://login.salesforce.com/services/oauth2/token";

    private final Client client;

    @Inject
    public SFDCAuthClient(Client client) {
        this.client = client;
    }

    public SFDCTokenResponse getTokenViaAuthorizationCode(String authorizationCode, String clientId,
                                                          String clientSecret, String redirectUri) {
        Response response = this.client.target(TOKEN_SERVICE_END_POINT)
                .queryParam("code", authorizationCode)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("redirect_uri", redirectUri)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(null));

        SFDCTokenResponse sfdcTokenResponse = response.readEntity(SFDCTokenResponse.class);
        if (sfdcTokenResponse.getError() != null) {
            throw new CastledRuntimeException(sfdcTokenResponse.getError());
        }
        return sfdcTokenResponse;
    }

    public SFDCIdResponse getSFDCIdResponse(String url, String accessToken) {
        return this.client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .get(SFDCIdResponse.class);
    }

    public SFDCTokenResponse getTokenViaRefreshToken(String refreshToken, String clientId, String clientSecret) {
        Response response = this.client.target(TOKEN_SERVICE_END_POINT)
                .queryParam("grant_type", "refresh_token")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("refresh_token", refreshToken)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(null));

        SFDCTokenResponse sfdcTokenResponse = response.readEntity(SFDCTokenResponse.class);
        if (sfdcTokenResponse.getError() != null) {
            throw new CastledRuntimeException(sfdcTokenResponse.getError());
        }
        return sfdcTokenResponse;
    }


}
