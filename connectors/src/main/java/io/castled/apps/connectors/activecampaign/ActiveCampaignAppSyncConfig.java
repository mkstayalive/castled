package io.castled.apps.connectors.activecampaign;

import io.castled.OptionsReferences;
import io.castled.apps.models.GenericSyncObject;
import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.forms.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@GroupActivator(dependencies = {"object"}, group = MappingFormGroups.SYNC_MODE)
public class ActiveCampaignAppSyncConfig extends AppSyncConfig {

    @FormField(title = "Select object to sync", type = FormFieldType.DROP_DOWN, group = MappingFormGroups.OBJECT,
            optionsRef = @OptionsRef(value = OptionsReferences.OBJECT, type = OptionsRefType.DYNAMIC))
    private GenericSyncObject object;
}
