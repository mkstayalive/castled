package io.castled.commons.streams;

import io.castled.exceptions.CastledException;
import io.castled.schema.IncompatibleValueException;
import io.castled.schema.SchemaMapper;
import io.castled.schema.models.FieldSchema;
import io.castled.schema.models.Tuple;
import io.castled.schema.models.RecordSchema;
import io.castled.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@Slf4j
public class JsonFileRecordInputStream implements FileRecordInputStream {

    private final Path filePath;
    private final SchemaMapper schemaMapper;
    private final RecordSchema recordSchema;
    private final BufferedReader bufferedReader;

    public JsonFileRecordInputStream(Path filePath, SchemaMapper schemaMapper,
                                     RecordSchema recordSchema, boolean gzipped) throws IOException {
        this.filePath = filePath;
        this.schemaMapper = schemaMapper;
        this.recordSchema = recordSchema;
        if (gzipped) {
            this.bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath.toFile()))));
        } else {
            this.bufferedReader = new BufferedReader(new InputStreamReader((new FileInputStream(filePath.toFile()))));
        }
    }

    @Override
    public Path getFilePath() {
        return filePath;
    }

    @Override
    public Tuple readRecord() throws Exception {
        String jsonRecord = this.bufferedReader.readLine();
        if (jsonRecord == null) {
            return null;
        }
        try {
            Map<String, Object> record = JsonUtils.jsonStringToMap(jsonRecord);
            Tuple.Builder recordBuilder = Tuple.builder();
            for (FieldSchema fieldSchema : recordSchema.getFieldSchemas()) {
                recordBuilder.put(fieldSchema, this.schemaMapper.transformValue(record.get(fieldSchema.getName()),
                        fieldSchema.getSchema()));
            }
            return recordBuilder.build();
        } catch (IncompatibleValueException e) {
            log.error("Failed to read json record in file {}", filePath.toString(), e);
            throw new CastledException(e);
        }
    }

}
