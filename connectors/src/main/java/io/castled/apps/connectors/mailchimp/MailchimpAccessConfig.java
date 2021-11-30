package io.castled.apps.connectors.mailchimp;

import io.castled.oauth.OAuthAccessConfig;
import io.castled.oauth.OAuthServiceType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MailchimpAccessConfig extends OAuthAccessConfig {

    private String datacenter;
    private String loginEmail;
    private String apiEndPoint;

    @Builder
    public MailchimpAccessConfig(String accessToken, String datacenter, String loginEmail, String apiEndPoint) {
        super(OAuthServiceType.MAILCHIMP, accessToken, null);
        this.datacenter = datacenter;
        this.loginEmail = loginEmail;
        this.apiEndPoint = apiEndPoint;
    }

}