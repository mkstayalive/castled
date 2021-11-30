package io.castled.commons.streams;

import com.google.inject.Singleton;
import io.castled.commons.models.FileFormat;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.SchemaMapper;
import io.castled.schema.models.RecordSchema;

import java.io.IOException;
import java.nio.file.Path;

@Singleton
public class FileRecordInputStreamFactory {

    public FileRecordInputStream getRecordInputStream(FileFormat fileFormat, Path filePath,
                                                      RecordSchema recordSchema, SchemaMapper schemaMapper, boolean gzipped) throws IOException {
        switch (fileFormat) {
            case CSV:
                return new CsvFileRecordInputStream(filePath, schemaMapper, recordSchema, gzipped);
            case JSON:
                return new JsonFileRecordInputStream(filePath, schemaMapper, recordSchema, gzipped);

            default:
                throw new CastledRuntimeException("Unknown file format " + fileFormat);
        }
    }
}
