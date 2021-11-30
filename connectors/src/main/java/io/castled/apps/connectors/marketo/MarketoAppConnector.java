package io.castled.apps.connectors.marketo;

import com.google.common.collect.Lists;
import io.castled.ObjectRegistry;
import io.castled.apps.ExternalAppConnector;
import io.castled.apps.ExternalAppType;
import io.castled.apps.models.ExternalAppSchema;
import io.castled.apps.models.GenericSyncObject;
import io.castled.commons.models.AppSyncMode;
import io.castled.forms.dtos.FormFieldOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MarketoAppConnector implements ExternalAppConnector<MarketoAppConfig, MarketoDataSink,
        MarketoAppSyncConfig> {

    @Override
    public List<FormFieldOption> getAllObjects(MarketoAppConfig config, MarketoAppSyncConfig mappingConfig) {
        return Arrays.stream(MarketoObject.values())
                .map(marketoObject -> new FormFieldOption(new GenericSyncObject(marketoObject.getName(),
                        ExternalAppType.MARKETO), marketoObject.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public MarketoDataSink getDataSink() {
        return ObjectRegistry.getInstance(MarketoDataSink.class);
    }

    @Override
    public ExternalAppSchema getSchema(MarketoAppConfig config, MarketoAppSyncConfig marketoAppSyncConfig) {
        MarketoBulkClient marketoBulkClient = new MarketoBulkClient(config);
        MarketoObject marketoObject = MarketoObject.getObjectByName(marketoAppSyncConfig.getObject().getObjectName());
        ObjectAttributesContainer attrsContainer = marketoBulkClient.getAttributes(marketoObject);
        return new ExternalAppSchema(
                MarketoUtils.getSchema(marketoObject, attrsContainer.getAttributes(marketoAppSyncConfig.getMode()),
                        attrsContainer.getDedupeAttrFieldMap()),
                attrsContainer.getPkEligibles(marketoAppSyncConfig.getMode())
        );
    }

    @Override
    public Class<MarketoAppSyncConfig> getMappingConfigType() {
        return MarketoAppSyncConfig.class;
    }

    @Override
    public List<AppSyncMode> getSyncModes(MarketoAppConfig config, MarketoAppSyncConfig mappingConfig) {
        return Lists.newArrayList(AppSyncMode.UPSERT, AppSyncMode.UPDATE);
    }

    @Override
    public Class<MarketoAppConfig> getAppConfigType() {
        return MarketoAppConfig.class;
    }

}
