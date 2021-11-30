package io.castled.apps.connectors.googlepubsub;

import io.castled.OptionsReferences;
import io.castled.apps.models.GenericSyncObject;
import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.forms.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@GroupActivator(dependencies = {"object"}, group = MappingFormGroups.SYNC_MODE)
public class GooglePubSubAppSyncConfig extends AppSyncConfig {

    @FormField(title = "Select Pub/Sub topic", type = FormFieldType.DROP_DOWN, group = MappingFormGroups.OBJECT, optionsRef = @OptionsRef(value = OptionsReferences.OBJECT, type = OptionsRefType.DYNAMIC))
    private GooglePubSubTopicSyncObject object;
}