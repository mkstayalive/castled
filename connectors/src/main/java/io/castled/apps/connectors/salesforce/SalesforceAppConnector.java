package io.castled.apps.connectors.salesforce;

import io.castled.ObjectRegistry;
import io.castled.apps.ExternalAppConnector;
import io.castled.apps.ExternalAppType;
import io.castled.apps.OAuthAppConfig;
import io.castled.apps.connectors.salesforce.client.SFDCRestClient;
import io.castled.apps.connectors.salesforce.client.SFDCUtils;
import io.castled.apps.connectors.salesforce.client.dtos.SFDCObjectField;
import io.castled.apps.models.ExternalAppSchema;
import io.castled.apps.models.GenericSyncObject;
import io.castled.apps.syncconfigs.GenericObjectRadioGroupConfig;
import io.castled.forms.dtos.FormFieldOption;

import java.util.List;
import java.util.stream.Collectors;

public class SalesforceAppConnector implements ExternalAppConnector<OAuthAppConfig, SalesforceDataSink,
        GenericObjectRadioGroupConfig> {

    @Override
    public List<FormFieldOption> getAllObjects(OAuthAppConfig config, GenericObjectRadioGroupConfig mappingConfig) {
        SFDCRestClient sfdcRestClient = new SFDCRestClient(config.getOAuthToken(),
                config.getClientConfig());
        return sfdcRestClient.getAllObjects().stream().map(sfdcObject -> new FormFieldOption
                (new GenericSyncObject(sfdcObject.getName(), ExternalAppType.SALESFORCE), sfdcObject.getName())).collect(Collectors.toList());
    }

    @Override
    public SalesforceDataSink getDataSink() {
        return ObjectRegistry.getInstance(SalesforceDataSink.class);
    }

    @Override
    public ExternalAppSchema getSchema(OAuthAppConfig salesforceAppConfig, GenericObjectRadioGroupConfig mappingConfig) {
        SFDCRestClient sfdcRestClient = new SFDCRestClient(salesforceAppConfig.getOAuthToken(),
                salesforceAppConfig.getClientConfig());
        List<SFDCObjectField> fields = sfdcRestClient.getObjectDetails(mappingConfig.getObject().getObjectName()).getFields();
        List<String> pkEligibles = fields.stream().filter(SFDCUtils::isDedupKeyEligible)
                .map(SFDCObjectField::getName).collect(Collectors.toList());
        return new ExternalAppSchema(SFDCUtils.getSchema(mappingConfig.getObject().getObjectName(), fields), pkEligibles);
    }

    @Override
    public Class<GenericObjectRadioGroupConfig> getMappingConfigType() {
        return GenericObjectRadioGroupConfig.class;
    }

    @Override
    public Class<OAuthAppConfig> getAppConfigType() {
        return OAuthAppConfig.class;
    }
}
