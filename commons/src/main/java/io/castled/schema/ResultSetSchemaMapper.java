package io.castled.schema;

import com.google.inject.Singleton;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.models.Tuple;
import io.castled.schema.models.*;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.util.TimeZone;

@Singleton
@Slf4j
public class ResultSetSchemaMapper {

    public RecordSchema getSchema(ResultSetMetaData resultSetMetaData) throws SQLException {
        RecordSchema.Builder schemaBuilder = RecordSchema.builder();
        for (int column = 1; column <= resultSetMetaData.getColumnCount(); column++) {
            String columnLabel = resultSetMetaData.getColumnLabel(column);
            int columnType = resultSetMetaData.getColumnType(column);
            String typeName = resultSetMetaData.getColumnTypeName(column);
            int scale = resultSetMetaData.getScale(column);
            int precision = resultSetMetaData.getPrecision(column);
            Schema columnSchema = getColumnSchema(columnType, typeName, scale, precision);
            if (columnSchema != null) {
                columnSchema.setOptional(true);
            }
            schemaBuilder.put(columnLabel, columnSchema);
        }
        return schemaBuilder.build();
    }

    public Tuple getRecord(ResultSet resultSet, RecordSchema recordSchema) {
        Tuple.Builder recordBuilder = Tuple.builder();
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            for (int column = 1; column <= resultSetMetaData.getColumnCount(); column++) {
                String columnLabel = resultSetMetaData.getColumnLabel(column);
                int columnType = resultSetMetaData.getColumnType(column);
                String typeName = resultSetMetaData.getColumnTypeName(column);
                Schema columnSchema = recordSchema.getSchema(columnLabel);
                if (columnSchema == null) {
                    continue;
                }
                Object columnValue = getColumnValue(resultSet.getObject(columnLabel), columnSchema, columnType, typeName);
                if (columnValue != null) {
                    recordBuilder.put(new FieldSchema(columnLabel, columnSchema), columnValue);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to read record from result set", e);
            throw new CastledRuntimeException(e.getMessage());
        }
        return recordBuilder.build();
    }

    public Object getColumnValue(Object value, Schema columnSchema, int columnType, String typeName) {
        if (value == null) {
            return null;
        }
        switch (columnType) {
            case Types.INTEGER:
                Integer integer = (Integer) value;
                return integer.longValue();
            case Types.TINYINT:
            case Types.SMALLINT:
                return ((Integer) value).shortValue();
            case Types.TIMESTAMP:
                return ((Timestamp) value).toLocalDateTime();
            case Types.DATE:
                return ((Date) value).toLocalDate();
            case Types.TIME:
                Time time = (Time) value;
                long epochMillis = time.getTime() + TimeZone.getDefault().getOffset(time.getTime());
                return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.of("UTC")).toLocalTime();
            default:
                return value;
        }
    }


    public Schema getColumnSchema(int columnType, String typeName, int scale, int precision) {
        switch (columnType) {
            case Types.BOOLEAN:
            case Types.BIT:
                return SchemaConstants.BOOL_SCHEMA;
            case Types.TINYINT:
            case Types.SMALLINT:
                return SchemaConstants.SHORT_SCHEMA;
            case Types.INTEGER:
            case Types.BIGINT:
                return SchemaConstants.LONG_SCHEMA;
            case Types.REAL:
                return SchemaConstants.FLOAT_SCHEMA;
            case Types.DOUBLE:
            case Types.FLOAT:
                return SchemaConstants.DOUBLE_SCHEMA;

            case Types.NUMERIC:
            case Types.DECIMAL:
                return DecimalSchema.builder().scale(scale).precision(precision).build();
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                return StringSchema.builder().maxLength(precision).build();

            case Types.TIME:
                return SchemaConstants.TIME_SCHEMA;

            case Types.DATE:
                return SchemaConstants.DATE_SCHEMA;

            case Types.TIMESTAMP:
                return SchemaConstants.TIMESTAMP_SCHEMA;

            case Types.TIMESTAMP_WITH_TIMEZONE:
                return SchemaConstants.ZONED_TIMESTAMP_SCHEMA;

            default:
                log.warn("Unhandled sql type " + columnType);
                return null;
        }
    }
}
