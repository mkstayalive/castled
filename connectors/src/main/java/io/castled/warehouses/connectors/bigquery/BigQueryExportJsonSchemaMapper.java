package io.castled.warehouses.connectors.bigquery;

import com.google.inject.Singleton;
import io.castled.schema.IncompatibleValueException;
import io.castled.schema.SchemaMapper;
import io.castled.schema.models.Schema;
import io.castled.schema.models.SchemaType;

import java.time.*;
import java.util.Date;

@Singleton
public class BigQueryExportJsonSchemaMapper extends SchemaMapper {

    public Object transformValue(Object value, Schema schema) throws IncompatibleValueException {
        if (schema.getType() == SchemaType.ZONED_TIMESTAMP) {
            return ZonedDateTime.parse((String) value);
        }
        if (schema.getType() == SchemaType.TIMESTAMP) {
            return LocalDateTime.parse((String) value);
        }
        if (schema.getType() == SchemaType.DATE) {
            return LocalDate.parse((String) value);
        }
        if (schema.getType() == SchemaType.TIME) {
            return LocalTime.parse((String) value);
        }
        return super.transformValue(value, schema);
    }
}
