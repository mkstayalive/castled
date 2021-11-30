package io.castled.warehouses.connectors.redshift;

import com.google.inject.Singleton;
import io.castled.schema.IncompatibleValueException;
import io.castled.schema.SchemaMapper;
import io.castled.schema.SchemaUtils;
import io.castled.schema.models.Schema;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Singleton
public class RedshiftCopySchemaMapper extends SchemaMapper {

    public Object transformValue(Object value, Schema schema) throws IncompatibleValueException {
        if (value == null) {
            return null;
        }
        if (SchemaUtils.isDateSchema(schema)) {
            new SimpleDateFormat("yyyy-MM-dd").format((Date) value);
        }
        if (SchemaUtils.isTimestampSchema(schema)) {
            return ((Date) value).getTime();
        }
        if (SchemaUtils.isDecimalSchema(schema)) {
            return ((BigDecimal) value).toPlainString();
        }

        if (SchemaUtils.isDateSchema(schema)) {
            new SimpleDateFormat("yyyy-MM-dd").format((Date) value);
        }
        if (SchemaUtils.isTimestampSchema(schema)) {
            return ((Date) value).getTime();
        }
        if (SchemaUtils.isDecimalSchema(schema)) {
            return ((BigDecimal) value).toPlainString();
        }
        return super.transformValue(value, schema);
    }
}
