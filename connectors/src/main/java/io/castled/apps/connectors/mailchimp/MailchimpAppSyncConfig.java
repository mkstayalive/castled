package io.castled.apps.connectors.mailchimp;

import io.castled.OptionsReferences;
import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.forms.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@GroupActivator(dependencies = {"object"}, group = MappingFormGroups.SYNC_MODE)
public class MailchimpAppSyncConfig extends AppSyncConfig {

    @FormField(title = "Select audience to sync", type = FormFieldType.DROP_DOWN, group = MappingFormGroups.OBJECT,
            optionsRef = @OptionsRef(value = OptionsReferences.OBJECT, type = OptionsRefType.DYNAMIC))
    private MailchimpAudienceSyncObject object;
}
