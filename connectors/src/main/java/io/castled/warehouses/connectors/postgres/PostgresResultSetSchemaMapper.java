package io.castled.warehouses.connectors.postgres;

import com.google.inject.Singleton;
import io.castled.schema.ResultSetSchemaMapper;
import io.castled.schema.models.Schema;

@Singleton
public class PostgresResultSetSchemaMapper extends ResultSetSchemaMapper {

    private static final String TIMESTAMPTZ = "timestamptz";

    public Schema getColumnSchema(int columnType, String typeName, int scale, int precision) {
        return super.getColumnSchema(columnType, typeName, scale, precision);
    }

    public Object getColumnValue(Object value, Schema columnSchema, int columnType, String typeName) {
        return super.getColumnValue(value, columnSchema, columnType, typeName);

    }
}
