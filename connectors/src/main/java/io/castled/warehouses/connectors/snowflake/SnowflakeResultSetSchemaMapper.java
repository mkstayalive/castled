package io.castled.warehouses.connectors.snowflake;

import com.google.inject.Singleton;
import io.castled.schema.ResultSetSchemaMapper;
import io.castled.schema.SchemaConstants;
import io.castled.schema.models.Schema;

@Singleton
public class SnowflakeResultSetSchemaMapper extends ResultSetSchemaMapper {

    public Schema getColumnSchema(int columnType, String typeName, int scale, int precision) {
        if (typeName.equals("TIMESTAMPTZ")) {
            return SchemaConstants.ZONED_TIMESTAMP_SCHEMA;
        }
        return super.getColumnSchema(columnType, typeName, scale, precision);
    }
}
