package io.castled.apps.connectors.customerio;

import io.castled.OptionsReferences;
import io.castled.apps.models.GenericSyncObject;
import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.forms.*;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@GroupActivator(dependencies = {"object"}, group = MappingFormGroups.SYNC_MODE)
@GroupActivator(dependencies = {"object"}, condition = "object.objectName == 'Event'", group = "eventTypeGroup")
@GroupActivator(dependencies = {"eventType","object"}, condition = "eventType == 'event' && object.objectName == 'Event'", group = "eventGroup")
@GroupActivator(dependencies = {"eventType","object"}, condition = "eventType == 'pageView' && object.objectName == 'Event'", group = "pageViewGroup")
@GroupActivator(dependencies = {"object"}, condition = "object.objectName == 'Person'", group = "personGroup")
@Getter
@Setter
public class CustomerIOAppSyncConfig extends AppSyncConfig {

    @FormField(title = "Select Object to sync", type = FormFieldType.DROP_DOWN, group = MappingFormGroups.OBJECT,
            optionsRef = @OptionsRef(value = OptionsReferences.OBJECT, type = OptionsRefType.DYNAMIC))
    private GenericSyncObject object;

    @FormField(title = "Select Event Type for tracking", type = FormFieldType.DROP_DOWN, group = "eventTypeGroup",
            optionsRef = @OptionsRef(value = OptionsReferences.CIO_EVENT_TYPES, type = OptionsRefType.DYNAMIC))
    private String eventType;

    @FormField(type = FormFieldType.DROP_DOWN, group ="eventGroup", title = "Warehouse Column identifying the Event Name", description = "Column which identifies the event name",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String eventName;

    @FormField(type = FormFieldType.DROP_DOWN, group ="pageViewGroup", title = "Warehouse Column identifying the URL of the page viewed", description = "How a source record will be uniquely identified",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String pageURL;

    @NotNull
    @FormField(type = FormFieldType.DROP_DOWN, group ="eventTypeGroup", title = "Warehouse Column uniquely identifying the Event Record", description = "Column which identifies the event ID",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String eventId;

    @NotNull
    @FormField(type = FormFieldType.DROP_DOWN, group ="eventTypeGroup", title = "Warehouse Column identifying Customer.io id (customer_id) of the person", description = "How a source record will be uniquely identified",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String customerId;

    @FormField(type = FormFieldType.DROP_DOWN, group ="eventTypeGroup", title = "Warehouse Column uniquely identifying the Event Timestamp", description = "Event timestamp",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String eventTimestamp;

    @NotNull
    @FormField(type = FormFieldType.DROP_DOWN, group ="personGroup", title = "Column uniquely identifying the Person Record", description = "How a source record will be uniquely identified",
            optionsRef = @OptionsRef(value = OptionsReferences.WAREHOUSE_COLUMNS, type = OptionsRefType.DYNAMIC))
    private String personIdentifier;

    @NotNull
    @FormField(type = FormFieldType.DROP_DOWN, group ="personGroup", title = "Matching Primary Key For Destination App Record", description = "How a source record will be uniquely identified",
            optionsRef = @OptionsRef(value = OptionsReferences.CIO_PRIMARY_KEYS, type = OptionsRefType.DYNAMIC))
    private String primaryKey;
}
