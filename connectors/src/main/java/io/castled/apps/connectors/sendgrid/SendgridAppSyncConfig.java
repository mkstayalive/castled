package io.castled.apps.connectors.sendgrid;

import io.castled.OptionsReferences;
import io.castled.apps.models.GenericSyncObject;
import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.forms.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@GroupActivator(dependencies = {"object"}, group = MappingFormGroups.SYNC_MODE)
public class SendgridAppSyncConfig extends AppSyncConfig {

    @FormField(title = "select sendgrid object to sync", type = FormFieldType.DROP_DOWN, group = MappingFormGroups.OBJECT,
            optionsRef = @OptionsRef(value = OptionsReferences.OBJECT, type = OptionsRefType.DYNAMIC))
    private GenericSyncObject object;

    @FormField(title = "Lists", type = FormFieldType.DROP_DOWN, description = "An array of Lists that this contact will be added to", group = MappingFormGroups.SUB_RESOURCE,
            optionsRef = @OptionsRef(value = OptionsReferences.SENDGRID_LISTS, type = OptionsRefType.DYNAMIC))
    private String listIds;
}
