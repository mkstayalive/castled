package io.castled.apps.connectors.salesforce;

import io.castled.oauth.OAuthAccessConfig;
import io.castled.oauth.OAuthServiceType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SalesforceAccessConfig extends OAuthAccessConfig {

    private String instanceUrl;
    private String userName;
    private String displayName;
    @Builder
    public SalesforceAccessConfig(String instanceUrl, String userName, String displayName, String accessToken, String refreshToken) {
        super(OAuthServiceType.SALESFORCE, accessToken, refreshToken);
        this.instanceUrl = instanceUrl;
        this.userName = userName;
        this.displayName = displayName;
    }
}
