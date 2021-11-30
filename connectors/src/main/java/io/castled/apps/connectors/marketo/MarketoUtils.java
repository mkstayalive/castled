package io.castled.apps.connectors.marketo;

import com.google.common.collect.Maps;
import io.castled.apps.connectors.marketo.dtos.GenericAttribute;
import io.castled.schema.SchemaConstants;
import io.castled.schema.models.RecordSchema;
import io.castled.schema.models.Schema;
import io.castled.schema.models.SchemaType;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
public class MarketoUtils {

    public static RecordSchema getSchema(MarketoObject marketoObject, List<GenericAttribute> attributes,
                                         Map<String, String> dedupeAttrsFieldMap) {
        RecordSchema.Builder schemaBuilder = RecordSchema.builder().name(marketoObject.getName());
        BiConsumer<GenericAttribute, MarketoObject> createSchema = (attrRef, objectRef) -> {
            try {
                Map<String, Object> params = Maps.newHashMap();
                // Stash the actual name in params, will be used when creating request object during insert/upsert.
                if (objectRef == MarketoObject.LEADS) {
                    params.put("name", attrRef.getRest().getName());
                } else {
                    params.put("name", attrRef.getName());
                    // Applicable only for dedupe or id keys, empty for all other keys
                    params.put("fieldName", dedupeAttrsFieldMap.get(attrRef.getName()));
                }
                schemaBuilder.put(attrRef.getDisplayName(), getFieldSchema(attrRef.getDataType()), params);
            } catch (Exception e) {
                throw e;
            }
        };
        attributes.stream().forEach(attrRef -> createSchema.accept(attrRef, marketoObject));
        return schemaBuilder.build();
    }

    public static Schema getFieldSchema(String dataType) {
        switch (dataType) {
            case "string":
            case "text":
            case "url":
            case "reference":
            case "currency":
                // TODO: Add a new schema const for phone and url
            case "phone":
                return SchemaConstants.OPTIONAL_STRING_SCHEMA;
            case "email":
                return SchemaConstants.OPTIONAL_EMAIL_SCHEMA;
            case "boolean":
                return SchemaConstants.OPTIONAL_BOOL_SCHEMA;
            case "integer":
                return SchemaConstants.OPTIONAL_LONG_SCHEMA;
            case "float":
                return SchemaConstants.OPTIONAL_FLOAT_SCHEMA;
            case "date":
                return SchemaConstants.OPTIONAL_DATE_SCHEMA;
            case "datetime":
                return SchemaConstants.OPTIONAL_ZONED_TIMESTAMP_SCHEMA;
            default:
                log.warn(String.format("Invalid data type %s", dataType));
                return null;
        }
    }

    public static Object formatValue(Object value, Schema schema) {
        if (value == null) {
            return null;
        }

        if (schema.getType() == SchemaType.DATE) {
            return DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format((LocalDate)value);
        } else if (schema.getType() == SchemaType.ZONED_TIMESTAMP) {
            return ((ZonedDateTime)value).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } else {
            return value;
        }
    }
}