package io.castled.apps.connectors.hubspot.schemaMappers;

import com.google.inject.Singleton;
import io.castled.schema.IncompatibleValueException;
import io.castled.schema.SchemaMapper;
import io.castled.schema.models.Schema;
import io.castled.schema.models.SchemaType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Singleton
public class HubspotApiSchemaMapper extends SchemaMapper {

    public Object transformValue(Object value, Schema schema) throws IncompatibleValueException {
        if (schema.getType() == SchemaType.TIMESTAMP) {
            LocalDateTime date = (LocalDateTime) value;
            return date.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
        }
        if (schema.getType() == SchemaType.ZONED_TIMESTAMP) {
            ZonedDateTime zonedDateTime = (ZonedDateTime) value;
            return zonedDateTime.toInstant().toEpochMilli();
        }
        return value;

    }
}
