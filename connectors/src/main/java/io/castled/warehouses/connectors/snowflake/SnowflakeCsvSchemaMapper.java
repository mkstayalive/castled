package io.castled.warehouses.connectors.snowflake;

import com.google.inject.Singleton;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.IncompatibleValueException;
import io.castled.schema.SchemaMapper;
import io.castled.schema.models.Schema;
import io.castled.schema.models.SchemaType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Singleton
public class SnowflakeCsvSchemaMapper extends SchemaMapper {

    public Object transformValue(Object value, Schema schema) throws IncompatibleValueException {
        if (value == null) {
            return null;
        }
        if (!(value instanceof String)) {
            throw new CastledRuntimeException("csv value needs to be string");
        }
        if (schema.getType() == SchemaType.DATE) {
            return LocalDate.parse((String) value);
        }
        if (schema.getType() == SchemaType.TIME) {
            return LocalTime.parse((String) value);

        }
        if (schema.getType() == SchemaType.TIMESTAMP) {
            return ZonedDateTime.parse((String) value, DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS XXXX")).toLocalDateTime();
        }
        if (schema.getType() == SchemaType.ZONED_TIMESTAMP) {
            return ZonedDateTime.parse((String) value, DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS XXXX"));
        }
        return super.transformValue(value, schema);
    }

}

