package io.castled.apps.connectors.marketo;

import com.google.common.hash.Hashing;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.utils.GenericKeyValueStore;
import io.castled.utils.StringUtils;
import io.castled.apps.connectors.marketo.dtos.MarketoTokenResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;

@Singleton
public class MarketoAuthClient {

    private final Client client;
    private final MarketoAppConfig appConfig;
    private final String MARKETO_NS = "marketo:";
    private final GenericKeyValueStore tokenStore;

    public MarketoAuthClient(MarketoAppConfig appConfig) {
        this.appConfig = appConfig;
        this.client = ObjectRegistry.getInstance(Client.class);
        this.tokenStore = ObjectRegistry.getInstance(GenericKeyValueStore.class);
    }

    public String getTokenViaRefreshToken() {
        final String TOKEN_SERVICE_END_POINT = String.format("%s/identity/oauth/token", appConfig.getBaseUrl());
        MarketoTokenResponse response = this.client.target(TOKEN_SERVICE_END_POINT)
                .queryParam("grant_type", "client_credentials")
                .queryParam("client_id", appConfig.getClientId())
                .queryParam("client_secret", appConfig.getClientSecret())
                .request(MediaType.APPLICATION_JSON)
                .get(MarketoTokenResponse.class);
        tokenStore.putKey(MARKETO_NS, getTokenKey(), response.getExpiresIn(), response.getAccessToken());
        return response.getAccessToken();
    }

    public String getToken() {
        String accessToken = tokenStore.getKey(MARKETO_NS, getTokenKey());
        if (StringUtils.isEmpty(accessToken)) {
            return getTokenViaRefreshToken();
        } else {
            return accessToken;
        }
    }

    private String getTokenKey() {
        String sha256hex = Hashing.sha256()
                .hashString(appConfig.getClientId() + appConfig.getClientSecret(), StandardCharsets.UTF_8)
                .toString();
        return sha256hex;
    }
}
