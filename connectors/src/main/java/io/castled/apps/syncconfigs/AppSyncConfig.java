package io.castled.apps.syncconfigs;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.castled.OptionsReferences;
import io.castled.apps.ExternalAppType;
import io.castled.apps.connectors.activecampaign.ActiveCampaignAppSyncConfig;
import io.castled.apps.connectors.customerio.CustomerIOAppSyncConfig;
import io.castled.apps.connectors.googleads.GoogleAdsAppSyncConfig;
import io.castled.apps.connectors.googlepubsub.GooglePubSubAppSyncConfig;
import io.castled.apps.connectors.kafka.KafkaAppSyncConfig;
import io.castled.apps.connectors.mailchimp.MailchimpAppSyncConfig;
import io.castled.apps.connectors.marketo.MarketoAppSyncConfig;
import io.castled.apps.connectors.mixpanel.MixpanelAppSyncConfig;
import io.castled.apps.connectors.sendgrid.SendgridAppSyncConfig;
import io.castled.apps.models.SubResource;
import io.castled.apps.models.SyncObject;
import io.castled.commons.models.AppSyncMode;
import io.castled.forms.*;
import lombok.Getter;
import lombok.Setter;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "appType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GoogleAdsAppSyncConfig.class, name = "GOOGLEADS"),
        @JsonSubTypes.Type(value = GenericObjectRadioGroupConfig.class, name = "SALESFORCE"),
        @JsonSubTypes.Type(value = GenericObjectRadioGroupConfig.class, name = "HUBSPOT"),
        @JsonSubTypes.Type(value = MailchimpAppSyncConfig.class, name = "MAILCHIMP"),
        @JsonSubTypes.Type(value = GenericObjectRadioGroupConfig.class, name = "INTERCOM"),
        @JsonSubTypes.Type(value = SendgridAppSyncConfig.class, name = "SENDGRID"),
        @JsonSubTypes.Type(value = MarketoAppSyncConfig.class, name = "MARKETO"),
        @JsonSubTypes.Type(value = KafkaAppSyncConfig.class, name = "KAFKA"),
        @JsonSubTypes.Type(value = ActiveCampaignAppSyncConfig.class, name = "ACTIVECAMPAIGN"),
        @JsonSubTypes.Type(value = CustomerIOAppSyncConfig.class, name = "CUSTOMERIO"),
        @JsonSubTypes.Type(value = GooglePubSubAppSyncConfig.class, name = "GOOGLEPUBSUB"),
        @JsonSubTypes.Type(value = MixpanelAppSyncConfig.class, name = "MIXPANEL")
})
@Getter
@Setter
public abstract class AppSyncConfig {

    private ExternalAppType appType;

    @FormField(type = FormFieldType.RADIO_GROUP, title = "Sync Mode", description = "Sync mode which controls whether records will be appended, updated or upserted", group = MappingFormGroups.SYNC_MODE,
            optionsRef = @OptionsRef(value = OptionsReferences.SYNC_MODE, type = OptionsRefType.DYNAMIC))
    private AppSyncMode mode;

    public abstract SyncObject getObject();

    public SubResource getSubResource() {
        return null;
    }

}
