package io.castled.apps.connectors.googleads;

import io.castled.OptionsReferences;
import io.castled.apps.models.GenericSyncObject;
import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.forms.*;
import lombok.Getter;
import lombok.Setter;


@GroupActivator(dependencies = {"accountId"}, group = "loginAccountId")
@GroupActivator(dependencies = {"loginCustomerId"}, group = MappingFormGroups.OBJECT)
@GroupActivator(dependencies = {"object","loginCustomerId"}, group = MappingFormGroups.SUB_RESOURCE)
@GroupActivator(dependencies = {"subResource"}, group = MappingFormGroups.SYNC_MODE)
@Getter
@Setter
public class GoogleAdsAppSyncConfig extends AppSyncConfig {

    @FormField(type = FormFieldType.DROP_DOWN, title = "Customer Id", description = "Google customer Id from the Google ads console eg: 788-9993-09993",
            optionsRef = @OptionsRef(value = OptionsReferences.GADS_ACCOUNT_ID, type = OptionsRefType.DYNAMIC))
    private String accountId;

    @FormField(type = FormFieldType.DROP_DOWN,title = "Auto Generated Login CustomerId",optionsRef = @OptionsRef(value = OptionsReferences.GADS_LOGIN_ACCOUNT_ID, type = OptionsRefType.DYNAMIC), group = "loginAccountId")
    private String loginCustomerId;

    @FormField(type = FormFieldType.RADIO_GROUP, title = "Resource", description = "Google Ads resource to sync the data",
            group = MappingFormGroups.OBJECT, optionsRef = @OptionsRef(value = OptionsReferences.OBJECT, type = OptionsRefType.DYNAMIC))
    private GenericSyncObject object;

    @FormField(type = FormFieldType.DROP_DOWN, title = "Sub resource", description = "Google Ads subresource to sync the data", group = MappingFormGroups.SUB_RESOURCE,
            optionsRef = @OptionsRef(value = OptionsReferences.SUB_RESOURCE, type = OptionsRefType.DYNAMIC))
    private GadsSubResource subResource;
}
