package io.castled.apps.connectors.hubspot;

import io.castled.apps.connectors.hubspot.client.dtos.HubspotProperty;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.SchemaConstants;
import io.castled.schema.models.DecimalSchema;
import io.castled.schema.models.RecordSchema;
import io.castled.schema.models.Schema;

import java.util.List;

public class HubspotUtils {

    public static RecordSchema getSchema(String object, List<HubspotProperty> hubspotProperties) {
        RecordSchema.Builder schemaBuilder = RecordSchema.builder().name(object);
        for (HubspotProperty hubspotProperty : hubspotProperties) {
            schemaBuilder.put(hubspotProperty.getName(), getFieldSchema(hubspotProperty));
        }
        return schemaBuilder.build();
    }

    public static Schema getFieldSchema(HubspotProperty hubspotProperty) {
        switch (hubspotProperty.getType()) {
            case DATETIME:
                return SchemaConstants.TIMESTAMP_SCHEMA;
            case DATE:
                return SchemaConstants.DATE_SCHEMA;
            case STRING:
            case ENUMERATION:
                return SchemaConstants.OPTIONAL_STRING_SCHEMA;
            case NUMBER:
                return DecimalSchema.builder().scale(9).precision(38).build();
            case BOOLEAN:
                return SchemaConstants.OPTIONAL_BOOL_SCHEMA;
            case PHONE_NUMBER:
                return SchemaConstants.OPTIONAL_LONG_SCHEMA;
            default:
                throw new CastledRuntimeException(String.format("Invalid hubspot property type %s", hubspotProperty.getType()));
        }
    }
}
