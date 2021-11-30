package io.castled.apps;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.castled.apps.connectors.activecampaign.ActiveCampaignAppConfig;
import io.castled.apps.connectors.customerio.CustomerIOAppConfig;
import io.castled.apps.connectors.googleads.GoogleAdsAppConfig;
import io.castled.apps.connectors.googlepubsub.GooglePubSubAppConfig;
import io.castled.apps.connectors.intercom.IntercomAppConfig;
import io.castled.apps.connectors.kafka.KafkaAppConfig;
import io.castled.apps.connectors.marketo.MarketoAppConfig;
import io.castled.apps.connectors.mixpanel.MixpanelAppConfig;
import io.castled.apps.connectors.sendgrid.SendgridAppConfig;
import lombok.Getter;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OAuthAppConfig.class, name = "SALESFORCE"),
        @JsonSubTypes.Type(value = OAuthAppConfig.class, name = "HUBSPOT"),
        @JsonSubTypes.Type(value = IntercomAppConfig.class, name = "INTERCOM"),
        @JsonSubTypes.Type(value = OAuthAppConfig.class, name = "MAILCHIMP"),
        @JsonSubTypes.Type(value = GoogleAdsAppConfig.class, name = "GOOGLEADS"),
        @JsonSubTypes.Type(value = SendgridAppConfig.class, name = "SENDGRID"),
        @JsonSubTypes.Type(value = MarketoAppConfig.class, name = "MARKETO"),
        @JsonSubTypes.Type(value = KafkaAppConfig.class, name = "KAFKA"),
        @JsonSubTypes.Type(value = ActiveCampaignAppConfig.class, name = "ACTIVECAMPAIGN"),
        @JsonSubTypes.Type(value = CustomerIOAppConfig.class, name = "CUSTOMERIO"),
        @JsonSubTypes.Type(value = GooglePubSubAppConfig.class, name = "GOOGLEPUBSUB"),
        @JsonSubTypes.Type(value = MixpanelAppConfig.class, name = "MIXPANEL")
})
@Getter
public abstract class AppConfig {

    private ExternalAppType type;
}