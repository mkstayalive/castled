package io.castled.apps.connectors.activecampaign;

import com.google.common.collect.ImmutableMap;
import io.castled.apps.connectors.activecampaign.constant.ActiveCampaignConstants;
import io.castled.apps.connectors.activecampaign.dto.CustomDataAttribute;
import io.castled.apps.connectors.activecampaign.models.ActiveCampaignModel;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.SchemaConstants;
import io.castled.schema.models.RecordSchema;
import io.castled.schema.models.Schema;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ActiveCampaignUtils {

    public static RecordSchema getSchema(ActiveCampaignObject activeCampaignObject, List<CustomDataAttribute> dataAttributes) {
        RecordSchema.Builder customerSchemaBuilder = RecordSchema.builder().name(activeCampaignObject.getName());

        for (ActiveCampaignObjectFields.CONTACTS_FIELDS field : ActiveCampaignObjectFields.CONTACTS_FIELDS.values()){
            customerSchemaBuilder.put(field.getFieldName(), field.getSchema(),
                    ImmutableMap.of(ActiveCampaignConstants.CUSTOM_FIELD_ID,field.getFieldTitle(),ActiveCampaignConstants.CUSTOM_FIELD_INDICATOR,false));
        }

        for (CustomDataAttribute dataAttribute : dataAttributes) {
            customerSchemaBuilder.put(dataAttribute.getTitle(),  Optional.ofNullable(getFieldSchema(activeCampaignObject, dataAttribute)).orElse(null),
                            ImmutableMap.of(ActiveCampaignConstants.CUSTOM_FIELD_ID,dataAttribute.getId(),ActiveCampaignConstants.CUSTOM_FIELD_INDICATOR,true));
        }
        return customerSchemaBuilder.build();
    }

    public static Schema getFieldSchema(ActiveCampaignObject activeCampaignObject, CustomDataAttribute dataAttribute) {


        switch (dataAttribute.getType()) {
            case "textarea":
                return SchemaConstants.OPTIONAL_STRING_SCHEMA;
            case "text":
                return SchemaConstants.OPTIONAL_STRING_SCHEMA;
            case "dropdown":
                return SchemaConstants.OPTIONAL_STRING_SCHEMA;
            case "radio":
                return SchemaConstants.OPTIONAL_BOOL_SCHEMA;
            case "checkbox":
                return SchemaConstants.OPTIONAL_BOOL_SCHEMA;
            case "listbox":
                return SchemaConstants.OPTIONAL_STRING_SCHEMA;
            case "hidden":
                return SchemaConstants.OPTIONAL_STRING_SCHEMA;
            case "date":
                return SchemaConstants.OPTIONAL_DATE_SCHEMA;
            case "datetime":
                return SchemaConstants.OPTIONAL_TIMESTAMP_SCHEMA;
            default:
                log.warn("Invalid data type %s", dataAttribute.getType());
                return null;
        }
    }
}
