package io.castled.apps.connectors.sendgrid;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.apps.ExternalAppConnector;
import io.castled.apps.ExternalAppType;

import io.castled.apps.connectors.sendgrid.dtos.ContactAttributesResponse;
import io.castled.apps.models.ExternalAppSchema;
import io.castled.apps.models.GenericSyncObject;
import io.castled.apps.connectors.sendgrid.dtos.ContactAttribute;
import io.castled.commons.models.AppSyncMode;
import io.castled.forms.dtos.FormFieldOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class SendgridAppConnector implements ExternalAppConnector<SendgridAppConfig, SendgridDataSink,
        SendgridAppSyncConfig> {

    @Override
    public List<FormFieldOption> getAllObjects(SendgridAppConfig config, SendgridAppSyncConfig mappingConfig) {
        return Arrays.stream(SendgridObject.values()).map(sendgridObject -> new FormFieldOption(new GenericSyncObject(sendgridObject.getName(),
                ExternalAppType.SENDGRID), sendgridObject.getName())).collect(Collectors.toList());
    }

    @Override
    public SendgridDataSink getDataSink() { return ObjectRegistry.getInstance(SendgridDataSink.class); }

    @Override
    public ExternalAppSchema getSchema(SendgridAppConfig config, SendgridAppSyncConfig mappingConfig) {
        SendgridRestClient sendgridRestClient = new SendgridRestClient(config);
        SendgridObject sendgridObject = SendgridObject.getObjectByName(mappingConfig.getObject().getObjectName());
        ContactAttributesResponse contactAttributes = sendgridRestClient.getContactAttributes();
        return new ExternalAppSchema(SendgridUtils.getSchema(sendgridObject, contactAttributes),
                Lists.newArrayList(ContactAttribute.EMAIL));
    }

    @Override
    public Class<SendgridAppSyncConfig> getMappingConfigType() {
        return SendgridAppSyncConfig.class;
    }

    @Override
    public List<AppSyncMode> getSyncModes(SendgridAppConfig config, SendgridAppSyncConfig mappingConfig) {
        return Lists.newArrayList(AppSyncMode.UPSERT);
    }

    @Override
    public Class<SendgridAppConfig> getAppConfigType() {
        return SendgridAppConfig.class;
    }
}
