package io.castled.apps.connectors.sendgrid;

import com.google.common.collect.Maps;
import io.castled.apps.connectors.sendgrid.dtos.ContactAttribute;
import io.castled.apps.connectors.sendgrid.dtos.ContactAttributesResponse;
import io.castled.schema.SchemaConstants;
import io.castled.schema.models.RecordSchema;
import io.castled.schema.models.Schema;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;
import java.util.Map;

@Slf4j
public class SendgridUtils {

    public static RecordSchema getSchema(SendgridObject sendgridObject, ContactAttributesResponse contactAttributes) {
        RecordSchema.Builder schemaBuilder = RecordSchema.builder().name(sendgridObject.getName());
        BiConsumer<ContactAttribute, Boolean> createSchema = (attrRef, isCustomAttr) -> {
            try {
                Map<String, Object> params = Maps.newHashMap();
                params.put("custom", isCustomAttr);
                schemaBuilder.put(attrRef.getName(), getFieldSchema(attrRef), params);
            } catch (Exception e) {
                throw e;
            }
        };
        contactAttributes.getReservedFields().stream().filter(attrRef -> getFieldSchema(attrRef) != null)
                .forEach(attrRef -> createSchema.accept(attrRef, false));
        contactAttributes.getCustomFields().stream().filter(attrRef -> getFieldSchema(attrRef) != null)
                .forEach(attrRef -> createSchema.accept(attrRef, true));
        return schemaBuilder.build();
    }

    public static Schema getFieldSchema(ContactAttribute contactAttribute) {
        switch (contactAttribute.getFieldType()) {
            case "Text":
                if (contactAttribute.getName().equals(ContactAttribute.EMAIL)) {
                    return SchemaConstants.EMAIL_SCHEMA;
                } else {
                    return SchemaConstants.OPTIONAL_STRING_SCHEMA;
                }
            case "Number":
                return SchemaConstants.OPTIONAL_LONG_SCHEMA;
            case "Date":
                return SchemaConstants.DATE_SCHEMA;
            default:
                log.warn(String.format("Invalid data type %s", contactAttribute.getFieldType()));
                return null;
        }
    }
}
