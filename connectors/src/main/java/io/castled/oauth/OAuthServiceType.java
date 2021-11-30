package io.castled.oauth;

import lombok.Getter;

public enum OAuthServiceType {
    SALESFORCE(OAuthProviderType.SALESFORCE),
    BIGQUERY(OAuthProviderType.GOOGLE),
    HUBSPOT(OAuthProviderType.HUBSPOT),
    GADS(OAuthProviderType.GOOGLE),
    MAILCHIMP(OAuthProviderType.MAILCHIMP);

    @Getter
    private final OAuthProviderType oAuthProviderType;

    OAuthServiceType(OAuthProviderType oAuthProviderType) {
        this.oAuthProviderType = oAuthProviderType;
    }


}
