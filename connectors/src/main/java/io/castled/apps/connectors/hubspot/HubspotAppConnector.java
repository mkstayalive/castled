package io.castled.apps.connectors.hubspot;

import io.castled.ObjectRegistry;
import io.castled.apps.ExternalAppConnector;
import io.castled.apps.ExternalAppType;
import io.castled.apps.OAuthAppConfig;
import io.castled.apps.connectors.hubspot.client.HubspotRestClient;
import io.castled.apps.connectors.hubspot.client.dtos.HubspotProperty;
import io.castled.apps.models.ExternalAppSchema;
import io.castled.apps.models.GenericSyncObject;
import io.castled.apps.syncconfigs.GenericObjectRadioGroupConfig;
import io.castled.forms.dtos.FormFieldOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HubspotAppConnector implements ExternalAppConnector<OAuthAppConfig, HubspotDataSink,
        GenericObjectRadioGroupConfig> {

    @Override
    public List<FormFieldOption> getAllObjects(OAuthAppConfig config, GenericObjectRadioGroupConfig mappingConfig) {
        return Arrays.stream(HubspotStandardObject.values())
                .map(hsObject -> new FormFieldOption(new GenericSyncObject(hsObject.getObjectName(),
                        ExternalAppType.HUBSPOT), hsObject.getObjectName())).collect(Collectors.toList());
    }


    @Override
    public ExternalAppSchema getSchema(OAuthAppConfig config, GenericObjectRadioGroupConfig mappingConfig) {
        HubspotRestClient hubspotRestClient = new HubspotRestClient(config.getOAuthToken(),
                config.getClientConfig());
        List<HubspotProperty> hubspotProperties = hubspotRestClient.getObjectProperties(mappingConfig.getObject().getObjectName())
                .stream().filter(hubspotProperty -> !hubspotProperty.isReadOnlyValue()).collect(Collectors.toList());

        return new ExternalAppSchema(HubspotUtils.getSchema(mappingConfig.getObject().getObjectName(), hubspotProperties),
                ObjectRegistry.getInstance(HubspotObjectFactory.class)
                        .getObjectHelper(mappingConfig.getObject().getObjectName()).dedupKeyEligibles(hubspotProperties));
    }

    @Override
    public Class<GenericObjectRadioGroupConfig> getMappingConfigType() {
        return GenericObjectRadioGroupConfig.class;
    }

    @Override
    public Class<OAuthAppConfig> getAppConfigType() {
        return OAuthAppConfig.class;
    }

    @Override
    public HubspotDataSink getDataSink() {
        return ObjectRegistry.getInstance(HubspotDataSink.class);
    }
}
