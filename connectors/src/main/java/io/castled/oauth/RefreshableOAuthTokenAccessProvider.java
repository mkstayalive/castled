package io.castled.oauth;

public interface RefreshableOAuthTokenAccessProvider {
    String refreshAccessToken(String refreshToken);
}
