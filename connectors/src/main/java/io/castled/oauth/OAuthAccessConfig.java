package io.castled.oauth;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.castled.apps.connectors.googleads.GadsAccessConfig;
import io.castled.apps.connectors.hubspot.HubspotAccessConfig;
import io.castled.apps.connectors.mailchimp.MailchimpAccessConfig;
import io.castled.apps.connectors.salesforce.SalesforceAccessConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "provider")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SalesforceAccessConfig.class, name = "SALESFORCE"),
        @JsonSubTypes.Type(value = HubspotAccessConfig.class, name = "HUBSPOT"),
        @JsonSubTypes.Type(value = GadsAccessConfig.class, name = "GADS"),
        @JsonSubTypes.Type(value = MailchimpAccessConfig.class, name = "MAILCHIMP")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
public abstract class OAuthAccessConfig {

    private OAuthServiceType provider;
    private String accessToken;
    private String refreshToken;
}
