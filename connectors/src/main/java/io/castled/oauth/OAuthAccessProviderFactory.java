package io.castled.oauth;

import io.castled.apps.connectors.googleads.GoogleAdsOAuthAccessProvider;
import io.castled.apps.connectors.hubspot.oauth.HubspotOAuthAccessProvider;
import io.castled.apps.connectors.mailchimp.MailchimpOAuthAccessProvider;
import io.castled.apps.connectors.salesforce.oauth.SalesforceOAuthAccessProvider;
import io.castled.exceptions.CastledRuntimeException;

import javax.inject.Singleton;

@Singleton
public class OAuthAccessProviderFactory {

    public OAuthAccessProvider getAccessProvider(OAuthServiceType oAuthServiceType,
                                                 OAuthClientConfig oAuthClientConfig) throws Exception {
        switch (oAuthServiceType) {
            case SALESFORCE:
                return new SalesforceOAuthAccessProvider(oAuthClientConfig);
            case HUBSPOT:
                return new HubspotOAuthAccessProvider(oAuthClientConfig);
            case GADS:
                return new GoogleAdsOAuthAccessProvider(oAuthClientConfig);
            case MAILCHIMP:
                return new MailchimpOAuthAccessProvider(oAuthClientConfig);
            default:
                throw new CastledRuntimeException(String.format("Invalid oauth access type %s", oAuthServiceType));
        }
    }
}
