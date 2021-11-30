package io.castled.schema;

import com.google.common.collect.Lists;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.models.*;

import java.util.List;
import java.util.stream.Collectors;

public class SchemaUtils {

    public static boolean isDateSchema(Schema schema) {
        return schema.getType() == SchemaType.DATE;
    }

    public static boolean isDecimalSchema(Schema schema) {
        return schema.getType() == SchemaType.DECIMAL;
    }

    public static boolean isTimestampSchema(Schema schema) {
        return schema.getType() == SchemaType.TIMESTAMP;
    }

    public static boolean isZonedTimestamp(Schema schema) {
        return schema.getType() == SchemaType.ZONED_TIMESTAMP;
    }

    public static boolean isTimeSchema(Schema schema) {
        return schema.getType() == SchemaType.TIME;
    }


    public static int getDecimalScale(Schema schema) {
        if (schema.getType() != SchemaType.DECIMAL) {
            throw new CastledRuntimeException("Schema should be a decimal schema");
        }
        DecimalSchema decimalSchema = (DecimalSchema) schema;
        return decimalSchema.getScale();
    }

    public static String getPrettyName(Schema schema) {
        return schema.getType().getDisplayName();
    }

    public static List<String> getFieldNames(Tuple record) {
        return record.getFields().stream().map(Field::getName).collect(Collectors.toList());
    }

    public static SimpleSchema transformToSimpleSchema(RecordSchema recordSchema) {
        if (recordSchema == null) {
            return null;
        }
        List<SchemaFieldDTO> schemaFields = Lists.newArrayList();
        for (FieldSchema fieldSchema : recordSchema.getFieldSchemas()) {
            schemaFields.add(new SchemaFieldDTO(fieldSchema.getName(), SchemaUtils.getPrettyName(fieldSchema.getSchema()), fieldSchema.getSchema().isOptional()));
        }
        return SimpleSchema.builder().schemaName(recordSchema.getName()).fields(schemaFields).build();
    }

    public static RecordSchema filterSchema(RecordSchema recordSchema, List<String> primaryKeys) {
        RecordSchema.Builder recordSchemaBuilder = RecordSchema.builder().name(recordSchema.getName());
        for (FieldSchema fieldSchema : recordSchema.getFieldSchemas().stream().filter(fieldSchema -> primaryKeys.contains(fieldSchema.getName()))
                .collect(Collectors.toList())) {
            recordSchemaBuilder.put(fieldSchema.getName(), fieldSchema.getSchema());
        }
        return recordSchemaBuilder.build();
    }
}
