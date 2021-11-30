package io.castled.apps.connectors.mixpanel;

import io.castled.OptionsReferences;
import io.castled.apps.models.GenericSyncObject;
import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.forms.*;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@GroupActivator(dependencies = {"object"}, group = MappingFormGroups.SYNC_MODE)
@GroupActivator(dependencies = {"object"}, condition = "object.objectName == 'Event'", group = "eventTypeGroup")
@GroupActivator(dependencies = {"object"}, condition = "object.objectName == 'User Profile'", group = "userProfileGroup")
@GroupActivator(dependencies = {"object"}, condition = "object.objectName == 'Group Profile'", group = "groupProfileGroup")
@Getter
@Setter
public class MixpanelAppSyncConfig extends AppSyncConfig {

    @FormField(title = "Select Object to sync", type = FormFieldType.DROP_DOWN, group = MappingFormGroups.OBJECT,
            optionsRef = @OptionsRef(value = OptionsReferences.OBJECT, type = OptionsRefType.DYNAMIC))
    private GenericSyncObject object;

    @FormField(type = FormFieldType.DROP_DOWN, group ="eventTypeGroup", title = "Warehouse Column uniquely identifying the Event", description = "Column which identifies the event name",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String eventID;

    @FormField(type = FormFieldType.DROP_DOWN, group ="eventTypeGroup", title = "Warehouse Column uniquely identifying Event Name", description = "Column which identifies the event name",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String eventName;

    @FormField(type = FormFieldType.DROP_DOWN, group ="eventTypeGroup", title = "Warehouse Column identifying the user associated with the event", description = "How a source record will be uniquely identified",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String distinctIDForEvent;

    @FormField(type = FormFieldType.DROP_DOWN, group ="eventTypeGroup", title = "Warehouse Column identifying Event Timestamp", description = "How a source record will be uniquely identified",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String eventTimeStamp;

    @FormField(type = FormFieldType.DROP_DOWN, group ="eventTypeGroup", title = "Warehouse Column identifying Geo IP", description = "How a source record will be uniquely identified",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String eventIP;

    @NotNull
    @FormField(type = FormFieldType.DROP_DOWN, group ="groupProfileGroup", title = "Warehouse Column uniquely identifying the Group", description = "Column which identifies the event ID",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String groupID;

    @NotNull
    @FormField(type = FormFieldType.TEXT_BOX, group ="groupProfileGroup", title = "Mixpanel Group Key", description = "Column which identifies the event ID")
    private String groupKey;

    @NotNull
    @FormField(type = FormFieldType.DROP_DOWN, group ="userProfileGroup", title = "Warehouse Column identifying the User", description = "How a source record will be uniquely identified",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String distinctID;

    @NotNull
    @FormField(type = FormFieldType.DROP_DOWN, group ="userProfileGroup", title = "Warehouse Column identifying the User First Name", description = "How a source record will be uniquely identified",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String firstName;

    @NotNull
    @FormField(type = FormFieldType.DROP_DOWN, group ="userProfileGroup", title = "Warehouse Column identifying the User Last Name", description = "How a source record will be uniquely identified",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String lastName;

    @NotNull
    @FormField(type = FormFieldType.DROP_DOWN, group ="userProfileGroup", title = "Warehouse Column identifying the User Email", description = "How a source record will be uniquely identified",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String userEmail;
}
