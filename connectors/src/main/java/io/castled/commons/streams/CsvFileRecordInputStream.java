package io.castled.commons.streams;

import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.IncompatibleValueException;
import io.castled.schema.SchemaMapper;
import io.castled.schema.models.FieldSchema;
import io.castled.schema.models.RecordSchema;
import io.castled.schema.models.Tuple;
import io.castled.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

@Slf4j
public class CsvFileRecordInputStream implements FileRecordInputStream {

    private final Path filePath;
    private final Iterator<CSVRecord> csvRecordIterator;
    private final SchemaMapper schemaMapper;
    private final RecordSchema recordSchema;

    public CsvFileRecordInputStream(Path filePath, SchemaMapper schemaMapper,
                                    RecordSchema recordSchema, boolean gzipped) throws IOException {
        this.filePath = filePath;
        this.schemaMapper = schemaMapper;
        this.recordSchema = recordSchema;

        if (gzipped) {
            this.csvRecordIterator = new CSVParser(new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath.toFile())))),
                    CSVFormat.RFC4180.withHeader().withSkipHeaderRecord()).iterator();
        } else {
            this.csvRecordIterator = new CSVParser(new BufferedReader(new FileReader(filePath.toFile())),
                    CSVFormat.RFC4180.withHeader().withSkipHeaderRecord()).iterator();
        }
    }

    @Override
    public Tuple readRecord() {
        if (!this.csvRecordIterator.hasNext()) {
            return null;
        }
        try {
            CSVRecord csvRecord = this.csvRecordIterator.next();
            Tuple.Builder recordBuilder = Tuple.builder();
            for (FieldSchema fieldSchema : recordSchema.getFieldSchemas()) {
                recordBuilder.put(fieldSchema, this.schemaMapper.transformValue(StringUtils.nullIfEmpty(csvRecord.get(fieldSchema.getName())), fieldSchema.getSchema()));
            }
            return recordBuilder.build();
        } catch (IncompatibleValueException e) {
            log.error("Failed to read csv record in file {}", filePath.toString(), e);
            throw new CastledRuntimeException(e);
        }
    }

    @Override
    public Path getFilePath() {
        return filePath;
    }
}
