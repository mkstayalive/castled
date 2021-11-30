package io.castled.apps.connectors.intercom;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.apps.ExternalAppConnector;
import io.castled.apps.ExternalAppType;
import io.castled.apps.models.ExternalAppSchema;
import io.castled.apps.models.GenericSyncObject;
import io.castled.apps.syncconfigs.GenericObjectRadioGroupConfig;
import io.castled.apps.connectors.intercom.client.IntercomObjectFields;
import io.castled.apps.connectors.intercom.client.IntercomRestClient;
import io.castled.apps.connectors.intercom.client.dtos.DataAttribute;
import io.castled.apps.connectors.intercom.client.models.IntercomModel;
import io.castled.commons.models.AppSyncMode;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.forms.dtos.FormFieldOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class IntercomAppConnector implements ExternalAppConnector<IntercomAppConfig, IntercomDataSink,
        GenericObjectRadioGroupConfig> {

    @Override
    public List<FormFieldOption> getAllObjects(IntercomAppConfig config, GenericObjectRadioGroupConfig mappingConfig) {
        return Arrays.stream(IntercomObject.values()).map(intercomObject -> new FormFieldOption(new GenericSyncObject(intercomObject.getName(),
                ExternalAppType.INTERCOM), intercomObject.getName())).collect(Collectors.toList());
    }

    @Override
    public IntercomDataSink getDataSink() {
        return ObjectRegistry.getInstance(IntercomDataSink.class);
    }

    @Override
    public ExternalAppSchema getSchema(IntercomAppConfig config, GenericObjectRadioGroupConfig mappingConfig) {
        IntercomRestClient intercomRestClient = new IntercomRestClient(config.getAccessToken());
        IntercomObject intercomObject = IntercomObject.getObjectByName(mappingConfig.getObject().getObjectName());
        IntercomModel intercomModel = IntercomUtils.getIntercomModel(intercomObject);
        List<DataAttribute> dataAttributes = intercomRestClient.listAttributes(intercomModel);
        return new ExternalAppSchema(IntercomUtils.getSchema(intercomObject, dataAttributes),
                getDeduplicationKeyEligibles(intercomModel));
    }

    @Override
    public Class<GenericObjectRadioGroupConfig> getMappingConfigType() {
        return GenericObjectRadioGroupConfig.class;
    }

    public List<String> getDeduplicationKeyEligibles(IntercomModel intercomModel) {
        switch (intercomModel) {
            case CONTACT:
                return Lists.newArrayList(IntercomObjectFields.EMAIL, IntercomObjectFields.EXTERNAL_USER_ID);
            case COMPANY:
                return Lists.newArrayList(IntercomObjectFields.COMPANY_ID);
            default:
                throw new CastledRuntimeException(String.format("Model %s not supported", intercomModel));
        }
    }

    public List<AppSyncMode> getSyncModes(IntercomAppConfig config, GenericObjectRadioGroupConfig mappingConfig) {
        return Lists.newArrayList(AppSyncMode.UPDATE, AppSyncMode.UPSERT);
    }

    @Override
    public Class<IntercomAppConfig> getAppConfigType() {
        return IntercomAppConfig.class;
    }
}
