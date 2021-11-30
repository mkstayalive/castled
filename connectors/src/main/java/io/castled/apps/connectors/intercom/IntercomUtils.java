package io.castled.apps.connectors.intercom;

import io.castled.apps.connectors.intercom.client.IntercomObjectFields;
import io.castled.apps.connectors.intercom.client.dtos.DataAttribute;
import io.castled.apps.connectors.intercom.client.models.IntercomModel;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.SchemaConstants;
import io.castled.schema.models.RecordSchema;
import io.castled.schema.models.Schema;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class IntercomUtils {

    /*
    convert intercom application objects to intercom core models
     */
    public static IntercomModel getIntercomModel(IntercomObject intercomObject) {
        switch (intercomObject) {
            case LEAD:
            case USER:
            case CONTACT:
                return IntercomModel.CONTACT;
            case COMPANY:
                return IntercomModel.COMPANY;
            default:
                throw new CastledRuntimeException(String.format("Invalid intercom object %s", intercomObject));
        }
    }

    public static RecordSchema getSchema(IntercomObject intercomObject, List<DataAttribute> dataAttributes) {
        RecordSchema.Builder schemaBuilder = RecordSchema.builder().name(intercomObject.getName());
        for (DataAttribute dataAttribute : dataAttributes) {
            Schema fieldSchema = getFieldSchema(intercomObject, dataAttribute);
            if (fieldSchema != null) {
                try {
                    schemaBuilder.put(dataAttribute.getName(), fieldSchema);
                } catch (Exception e) {
                    throw e;
                }
            }
        }
        return schemaBuilder.build();
    }

    public static Schema getFieldSchema(IntercomObject intercomObject, DataAttribute dataAttribute) {

        if (!dataAttribute.isApiWritable() && !dataAttribute.getName().equals("company_id")) {
            return null;
        }
        if (intercomObject == IntercomObject.USER || intercomObject == IntercomObject.LEAD) {
            if (dataAttribute.getName().equals(IntercomObjectFields.ROLE)) {
                //role would be populated based on whether its user or lead
                return null;
            }
        }
        if (intercomObject == IntercomObject.CONTACT && dataAttribute.getName().equals(IntercomObjectFields.ROLE)) {
            return SchemaConstants.STRING_SCHEMA;

        }
        switch (dataAttribute.getDataType()) {
            case "string":
                return SchemaConstants.OPTIONAL_STRING_SCHEMA;
            case "integer":
                return SchemaConstants.OPTIONAL_LONG_SCHEMA;
            case "float":
                return SchemaConstants.OPTIONAL_DOUBLE_SCHEMA;
            case "boolean":
                return SchemaConstants.OPTIONAL_BOOL_SCHEMA;
            case "date":
                return SchemaConstants.ZONED_TIMESTAMP_SCHEMA;
            default:
                log.warn(String.format("Invalid data type %s", dataAttribute.getDataType()));
                return null;
        }
    }
}
