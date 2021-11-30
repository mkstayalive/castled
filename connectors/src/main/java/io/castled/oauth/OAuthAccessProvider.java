package io.castled.oauth;


public interface OAuthAccessProvider {

    String getAuthorizationUrl(String stateId, String redirectUri, String clientId);

    Long persistAccessConfig(String authorizationCode, String redirectUri);
}
