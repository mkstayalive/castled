package io.castled.warehouses.connectors.redshift;

import com.google.inject.Singleton;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.IncompatibleValueException;
import io.castled.schema.SchemaMapper;
import io.castled.schema.SchemaUtils;
import io.castled.schema.models.Schema;
import io.castled.schema.models.SchemaType;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Singleton
public class RedshiftCsvSchemaMapper extends SchemaMapper {

    public Object transformValue(Object value, Schema schema) throws IncompatibleValueException {
        if (value == null) {
            return value;
        }
        if (!(value instanceof String)) {
            throw new CastledRuntimeException("Value needs to be an instance of String");
        }

        String valueAsStr = (String) value;
        if (StringUtils.isEmpty(valueAsStr)) {
            return null;
        }
        if (SchemaUtils.isDateSchema(schema)) {
            return LocalDate.parse((String) value);
        }

        if (SchemaUtils.isTimestampSchema(schema)) {
            return LocalDateTime.parse(valueAsStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        if (schema.getType() == SchemaType.ZONED_TIMESTAMP) {
            return ZonedDateTime.parse(valueAsStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX"));
        }

        if (schema.getType() == SchemaType.TIME) {
            try {
                return LocalTime.parse(valueAsStr);
            } catch (DateTimeParseException e) {
                return LocalTime.parse(valueAsStr, DateTimeFormatter.ofPattern("HH:mm:ssXXX"));
            }
        }

        if (schema.getType().equals(SchemaType.BOOLEAN)) {
            if (valueAsStr.equals("t")) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;

        }
        return super.transformValue(value, schema);
    }
}
