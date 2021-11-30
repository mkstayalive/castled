package io.castled.apps.connectors.hubspot.schemaMappers;

import com.google.inject.Singleton;
import io.castled.schema.IncompatibleValueException;
import io.castled.schema.SchemaMapper;
import io.castled.schema.models.Schema;
import io.castled.schema.models.SchemaType;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Singleton
public class HubspotPropertySchemaMapper extends SchemaMapper {

    public Object transformValue(Object value, Schema schema) throws IncompatibleValueException {
        if (schema.getType() == SchemaType.DATE) {
            String dateString = (String) value;
            return LocalDate.parse(dateString);
        }
        if (schema.getType() == SchemaType.ZONED_TIMESTAMP) {
            String dateString = (String) value;
            return ZonedDateTime.parse(dateString).toInstant();
        }
        return super.transformValue(value, schema);
    }
}
