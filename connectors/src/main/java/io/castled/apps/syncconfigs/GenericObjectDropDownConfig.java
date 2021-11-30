package io.castled.apps.syncconfigs;

import io.castled.OptionsReferences;
import io.castled.apps.models.GenericSyncObject;
import io.castled.forms.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@GroupActivator(dependencies = {"object"}, group = MappingFormGroups.SYNC_MODE)
public class GenericObjectDropDownConfig extends AppSyncConfig {

    @FormField(type = FormFieldType.DROP_DOWN, group = MappingFormGroups.OBJECT, optionsRef = @OptionsRef(value = OptionsReferences.OBJECT, type = OptionsRefType.DYNAMIC))
    private GenericSyncObject object;
}
