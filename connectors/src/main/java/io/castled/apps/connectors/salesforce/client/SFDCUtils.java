package io.castled.apps.connectors.salesforce.client;

import io.castled.apps.connectors.salesforce.SalesforceAccessConfig;
import io.castled.apps.connectors.salesforce.client.dtos.SFDCObjectField;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.SchemaConstants;
import io.castled.schema.models.RecordSchema;
import io.castled.schema.models.Schema;

import java.util.List;

public class SFDCUtils {

    private static final String API_VERSION = "47.0";

    public static String getRestEndPoint(SalesforceAccessConfig accessConfig) {
        return String.format("%s/services/data/%s", accessConfig.getInstanceUrl(), "v" + API_VERSION);
    }

    public static String getBulkApiEndPoint(SalesforceAccessConfig accessConfig) {
        return String.format("%s/services/async/%s", accessConfig.getInstanceUrl(), API_VERSION);
    }

    public static RecordSchema getSchema(String object, List<SFDCObjectField> sfdcObjectFields) {
        RecordSchema.Builder recordBuilder = RecordSchema.builder().name(object);
        for (SFDCObjectField objectField : sfdcObjectFields) {
            Schema fieldSchema = getFieldSchema(objectField);
            if (fieldSchema != null) {
                recordBuilder.put(objectField.getName(), fieldSchema);
            }
        }
        return recordBuilder.build();
    }

    public static Schema getFieldSchema(SFDCObjectField sfdcObjectField) {
        String soapType = sfdcObjectField.getSoapType().split(":")[1];
        SFDCSoapType sfdcSoapType = SFDCSoapType.fromName(soapType);
        if (sfdcSoapType == null) {
            return null;
        }
        Schema schema = null;
        switch (sfdcSoapType) {
            case DATE:
                schema = SchemaConstants.DATE_SCHEMA;
                break;
            case DATETIME:
                schema = SchemaConstants.ZONED_TIMESTAMP_SCHEMA;
                break;
            case INTEGER:
                schema = SchemaConstants.LONG_SCHEMA;
                break;
            case TIME:
                schema = SchemaConstants.TIME_SCHEMA;
                break;
            case STRING:
            case ID:
                schema = SchemaConstants.STRING_SCHEMA;
                break;
            case BOOLEAN:
                schema = SchemaConstants.BOOL_SCHEMA;
                break;
            case DOUBLE:
                if (sfdcObjectField.getScale() > 0) {
                    schema = SchemaConstants.DOUBLE_SCHEMA;
                } else {
                    schema = SchemaConstants.LONG_SCHEMA;
                }
                break;
            default:
                throw new CastledRuntimeException("Invalid soapType " + sfdcSoapType);
        }

        if (sfdcObjectField.isNillable()) {
            schema.setOptional(true);
        }
        return schema;
    }

    public static boolean isDedupKeyEligible(SFDCObjectField sfdcObjectField) {
        return sfdcObjectField.isExternalId() || sfdcObjectField.isIdLookup()
                || sfdcObjectField.isUnique();
    }
}
