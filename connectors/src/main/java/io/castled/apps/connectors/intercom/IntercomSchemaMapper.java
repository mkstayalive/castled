package io.castled.apps.connectors.intercom;

import com.google.inject.Singleton;
import io.castled.schema.IncompatibleValueException;
import io.castled.schema.SchemaMapper;
import io.castled.schema.SchemaUtils;
import io.castled.schema.models.Schema;

import java.util.Date;

@Singleton
public class IntercomSchemaMapper extends SchemaMapper {

    public Object transformValue(Object value, Schema schema) throws IncompatibleValueException {
        if (SchemaUtils.isTimestampSchema(schema)) {
            Date date = (Date) value;
            return date.getTime();
        }
        return super.transformValue(value, schema);
    }
}
