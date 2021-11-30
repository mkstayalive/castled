package io.castled.apps;

import com.google.common.collect.ImmutableMap;
import io.castled.commons.models.AccessType;
import io.castled.constants.ConnectorConstants;
import io.castled.oauth.OAuthServiceType;

import java.util.Map;
import java.util.Optional;

public enum ExternalAppType {

    SALESFORCE(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.OAUTH)
            .put(ConnectorConstants.TITLE, "Salesforce")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/apps/salesforce.png")
            .put(ConnectorConstants.OAUTH_SERVICE, OAuthServiceType.SALESFORCE)
            .build()),
    HUBSPOT(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.OAUTH)
            .put(ConnectorConstants.TITLE, "Hubspot")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/apps/hubspot.svg")
            .put(ConnectorConstants.OAUTH_SERVICE, OAuthServiceType.HUBSPOT)
            .build()),
    INTERCOM(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.API_KEY)
            .put(ConnectorConstants.TITLE, "Intercom")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/apps/intercom.png")
            .build()),
    GOOGLEADS(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.OAUTH)
            .put(ConnectorConstants.TITLE, "Google Ads")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/apps/gads.png")
            .put(ConnectorConstants.OAUTH_SERVICE, OAuthServiceType.GADS)
            .build()),
    MAILCHIMP(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.OAUTH)
            .put(ConnectorConstants.TITLE, "Mailchimp")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/apps/mailchimp.png")
            .put(ConnectorConstants.OAUTH_SERVICE, OAuthServiceType.MAILCHIMP)
            .build()),
    SENDGRID(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.API_KEY)
            .put(ConnectorConstants.TITLE, "Sendgrid")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/apps/sendgrid.png")
            .put(ConnectorConstants.DOC_URL, "https://docs.castled.io/getting-started/Destinations/configure-sendgrid")
            .build()),
    MARKETO(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.API_KEY)
            .put(ConnectorConstants.TITLE, "Marketo")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/apps/marketo.png")
            .put(ConnectorConstants.DOC_URL, "https://docs.castled.io/getting-started/Destinations/configure-marketo")
            .build()),
    ACTIVECAMPAIGN(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.API_KEY)
            .put(ConnectorConstants.TITLE, "ActiveCampaign")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/apps/active_campaign.png")
            .build()),
    KAFKA(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.API_KEY)
            .put(ConnectorConstants.TITLE, "Kafka")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/apps/kafka.png")
            .build()),
    CUSTOMERIO(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.API_KEY)
            .put(ConnectorConstants.TITLE, "Customer.io")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/apps/customerio.png")
            .build()),
    GOOGLEPUBSUB(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.API_KEY)
            .put(ConnectorConstants.TITLE, "Google Pub/Sub")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/apps/googlepubsub.png")
            .build()),
    MIXPANEL(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.API_KEY)
            .put(ConnectorConstants.TITLE, "Mixpanel")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/apps/mixpanel.png")
            .build());

    private final Map<String, Object> properties;

    ExternalAppType(Map<String, Object> properties) {
        this.properties = properties;
    }

    public AccessType getAccessType() {
        return (AccessType) properties.get(ConnectorConstants.ACCESS_TYPE);
    }

    public String title() {
        return (String) properties.get(ConnectorConstants.TITLE);
    }

    public String logoUrl() {
        return (String) properties.get(ConnectorConstants.LOGO_URL);
    }

    public String docUrl() {
        return (String) properties.get(ConnectorConstants.DOC_URL);

    }

    public OAuthServiceType oauthServiceType() {
        OAuthServiceType oauthService = (OAuthServiceType) properties.get(ConnectorConstants.OAUTH_SERVICE);
        return Optional.ofNullable(oauthService).orElse(null);
    }


}
