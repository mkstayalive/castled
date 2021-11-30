package io.castled.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.collect.Lists;
import io.castled.exceptions.CastledRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
public abstract class GoogleOAuthAccessProvider extends BaseOauthAccessProvider {

    public static final String USERINFO_EMAIL = "https://www.googleapis.com/auth/userinfo.email";
    private final GoogleAuthorizationCodeFlow authorizationCodeFlow;

    public GoogleOAuthAccessProvider(OAuthClientConfig oAuthClientConfig) throws Exception {
        this.authorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                oAuthClientConfig.getClientId(),
                oAuthClientConfig.getClientSecret(), getAuthorizationScopes())
                .setAccessType("offline").setApprovalPrompt("force")
                .build();
    }

    public List<String> getAuthorizationScopes() {
        List<String> scopes = Lists.newArrayList();
        if (!scopes.contains(USERINFO_EMAIL)) {
            scopes.add(USERINFO_EMAIL);
        }
        scopes.addAll(getScopes());
        return scopes;
    }

    public abstract List<String> getScopes();

    @Override
    public String getAuthorizationUrl(String stateId, String redirectUri, String clientId) {
        return authorizationCodeFlow.newAuthorizationUrl().setRedirectUri(redirectUri).setState(stateId).build();
    }

    @Override
    public OAuthAccessConfig getAccessConfig(String authorizationCode, String redirectUri) {
        try {
            GoogleTokenResponse response = this.authorizationCodeFlow.newTokenRequest(authorizationCode).setRedirectUri(redirectUri).execute();
            String accessToken = response.getAccessToken();
            String refreshToken = response.getRefreshToken();
            Map<String, Object> claimsMap = new JwtConsumerBuilder()
                    .setSkipSignatureVerification()
                    .setSkipDefaultAudienceValidation()
                    .build().processToClaims(response.getIdToken()).getClaimsMap();

            String email = (String) claimsMap.get("email");
            return doGetAccessConfig(accessToken, refreshToken, email);

        } catch (Exception e) {
            log.error("Get Access config failed", e);
            throw new CastledRuntimeException(e);
        }
    }

    public abstract OAuthAccessConfig doGetAccessConfig(String accessToken, String refreshToken, String email);
}
