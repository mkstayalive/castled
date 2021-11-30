package io.castled.errors;

import io.castled.ObjectRegistry;
import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.CastledErrorTracker;
import io.castled.schema.IncompatibleValueException;
import io.castled.schema.SchemaConstants;
import io.castled.schema.SchemaMapper;
import io.castled.schema.models.FieldSchema;
import io.castled.schema.models.RecordSchema;
import io.castled.schema.models.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class SchemaMappedErrorTracker implements CastledErrorTracker {

    private final RecordSchema targetSchema;
    private final CastledErrorTracker castledErrorTracker;
    private final SchemaMapper schemaMapper;
    private final Map<String, String> targetSourceMapping;

    public SchemaMappedErrorTracker(CastledErrorTracker castledErrorTracker, RecordSchema targetSchema, Map<String, String> targetSourceMapping) {
        this.castledErrorTracker = castledErrorTracker;
        this.targetSchema = targetSchema;
        this.schemaMapper = ObjectRegistry.getInstance(SchemaMapper.class);
        this.targetSourceMapping = targetSourceMapping;
    }

    @Override
    public void writeError(Tuple record, CastledError pipelineError) throws Exception {
        Tuple.Builder recordBuilder = Tuple.builder();
        for (FieldSchema fieldSchema : targetSchema.getFieldSchemas()) {
            String sourceField = targetSourceMapping.get(fieldSchema.getName());
            if (sourceField != null) {
                Object value = record.getValue(sourceField);
                try {
                    recordBuilder.put(fieldSchema, schemaMapper.transformValue(value, fieldSchema.getSchema()));
                } catch (IncompatibleValueException e) {
                    recordBuilder.put(new FieldSchema(fieldSchema.getName(), SchemaConstants.STRING_SCHEMA), value.toString());
                }
            }
        }
        castledErrorTracker.writeError(recordBuilder.build(), pipelineError);
    }

    @Override
    public void flushErrors() throws Exception {
        this.castledErrorTracker.flushErrors();
    }
}
