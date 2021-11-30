package io.castled.pipelines;

import io.castled.ObjectRegistry;
import io.castled.commons.streams.RecordOutputStream;
import io.castled.schema.SchemaMapper;
import io.castled.schema.models.FieldSchema;
import io.castled.schema.models.RecordSchema;
import io.castled.schema.models.Tuple;

import java.util.Map;

public class SchemaMappedRecordOutputStream implements RecordOutputStream {

    private final RecordSchema targetSchema;
    private final RecordOutputStream recordOutputStream;
    private final Map<String, String> targetSourceMapping;
    private final SchemaMapper schemaMapper;

    public SchemaMappedRecordOutputStream(RecordSchema targetSchema, RecordOutputStream recordOutputStream,
                                          Map<String, String> targetSourceMapping) {
        this.targetSchema = targetSchema;
        this.recordOutputStream = recordOutputStream;
        this.targetSourceMapping = targetSourceMapping;
        this.schemaMapper = ObjectRegistry.getInstance(SchemaMapper.class);
    }

    @Override
    public void writeRecord(Tuple inputRecord) throws Exception {

        Tuple.Builder targetRecordBuilder = Tuple.builder();
        for (FieldSchema field : targetSchema.getFieldSchemas()) {
            String sourceField = targetSourceMapping.get(field.getName());
            if (sourceField != null) {
                targetRecordBuilder.put(field, schemaMapper.transformValue(inputRecord.getValue(sourceField), field.getSchema()));
            }
        }
        recordOutputStream.writeRecord(targetRecordBuilder.build());
    }

    @Override
    public void flush() throws Exception {
        this.recordOutputStream.flush();
    }
}
