package io.castled.apps.connectors.activecampaign;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.apps.ExternalAppConnector;
import io.castled.apps.ExternalAppType;
import io.castled.apps.connectors.activecampaign.client.ActiveCampaignRestClient;
import io.castled.apps.connectors.activecampaign.dto.CustomDataAttribute;
import io.castled.apps.models.ExternalAppSchema;
import io.castled.apps.models.GenericSyncObject;
import io.castled.commons.models.AppSyncMode;
import io.castled.forms.dtos.FormFieldOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ActiveCampaignAppConnector implements ExternalAppConnector<ActiveCampaignAppConfig,
        ActiveCampaignDataSink, ActiveCampaignAppSyncConfig> {

    @Override
    public List<FormFieldOption> getAllObjects(ActiveCampaignAppConfig config, ActiveCampaignAppSyncConfig activeCampaignAppSyncConfig) {

        return Arrays.stream(ActiveCampaignObject.values()).map(activeCampaignObject -> new FormFieldOption(new GenericSyncObject(activeCampaignObject.getName(),
                ExternalAppType.ACTIVECAMPAIGN), activeCampaignObject.getName())).collect(Collectors.toList());
    }

    @Override
    public ActiveCampaignDataSink getDataSink() {
        return ObjectRegistry.getInstance(ActiveCampaignDataSink.class);
    }

    @Override
    public ExternalAppSchema getSchema(ActiveCampaignAppConfig config, ActiveCampaignAppSyncConfig mailchimpAppSyncConfig) {

        ActiveCampaignRestClient activeCampaignRestClient = new ActiveCampaignRestClient(config.getApiURL(),config.getApiKey());
        List<CustomDataAttribute>  customAttributes = activeCampaignRestClient.getContactCustomFields();

        return new ExternalAppSchema(ActiveCampaignUtils.getSchema(ActiveCampaignObject.CONTACT,customAttributes),
                Lists.newArrayList(ActiveCampaignObjectFields.CONTACTS_FIELDS.EMAIL.getFieldName()));
    }

    public List<AppSyncMode> getSyncModes(ActiveCampaignAppConfig config, ActiveCampaignAppSyncConfig activeCampaignAppSyncConfig) {
        return Lists.newArrayList(AppSyncMode.UPSERT);
    }

    public Class<ActiveCampaignAppSyncConfig> getMappingConfigType() {
        return ActiveCampaignAppSyncConfig.class;
    }

    @Override
    public Class<ActiveCampaignAppConfig> getAppConfigType() {
        return ActiveCampaignAppConfig.class;
    }
}
