package io.castled.utils;

import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.IncompatibleValueException;
import io.castled.schema.SchemaMapper;
import io.castled.schema.models.FieldSchema;
import io.castled.schema.models.Tuple;
import io.castled.schema.models.RecordSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

@Slf4j
public class CsvStructParser {

    private final Path filePath;
    private final Iterator<CSVRecord> csvRecordIterator;
    private final SchemaMapper schemaMapper;
    private final RecordSchema recordSchema;
    private long readLineNumber = 0;

    public CsvStructParser(Path filePath, SchemaMapper schemaMapper,
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
        readLineNumber = 1;
    }

    public Tuple getRecord(long lineNumber) {
        try {
            while (this.csvRecordIterator.hasNext()) {
                CSVRecord csvRecord = this.csvRecordIterator.next();
                readLineNumber++;
                if (readLineNumber > lineNumber) {
                    throw new CastledRuntimeException(String.format("Reader has passed reading line number %d", lineNumber));
                }
                if (readLineNumber == lineNumber) {
                    Tuple.Builder recordBuilder = Tuple.builder();
                    for (FieldSchema field : recordSchema.getFieldSchemas()) {
                        recordBuilder.put(field, this.schemaMapper.transformValue(csvRecord.get(field.getName()), field.getSchema()));
                    }
                    return recordBuilder.build();
                }
            }
            throw new CastledRuntimeException(String.format("Line number %d not present in file", lineNumber));
        } catch (IncompatibleValueException e) {
            log.error("Failed to read csv record in file {}", filePath.toString(), e);
            throw new CastledRuntimeException(e);
        }

    }


}
