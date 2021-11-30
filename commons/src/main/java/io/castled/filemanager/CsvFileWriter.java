package io.castled.filemanager;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class CsvFileWriter {

    private final long maxRecords;
    private final Path fileDirectory;
    private final Supplier<String> fileNameSupplier;
    private final List<String> headerFields;
    private volatile CSVPrinter csvPrinter;
    private long recordCount = 0;


    public CsvFileWriter(long maxRecords, Path fileDirectory, Supplier<String> fileNameSupplier,
                         List<String> headerFields) throws IOException {
        this.maxRecords = maxRecords;
        this.fileDirectory = fileDirectory;
        this.fileNameSupplier = fileNameSupplier;
        this.headerFields = headerFields;
        if (!Files.exists(fileDirectory)) {
            Files.createDirectories(fileDirectory);
        }
    }

    public void writeRecord(List<Object> fieldValues) throws IOException {
        if (csvPrinter == null) {
            String fileName = Optional.ofNullable(fileNameSupplier).map(Supplier::get).orElse(UUID.randomUUID().toString());
            this.csvPrinter = new CSVPrinter(new BufferedWriter(new FileWriter(fileDirectory.resolve(fileName).toFile())), CSVFormat.DEFAULT
                    .withHeader(headerFields.toArray(new String[0])).withQuoteMode(QuoteMode.ALL));
        }
        this.csvPrinter.printRecord(fieldValues);
        recordCount++;
        if (recordCount > maxRecords) {
            close();
        }
    }

    public void close() throws IOException {
        if (this.csvPrinter != null) {
            this.csvPrinter.flush();
            this.csvPrinter.close();
            this.csvPrinter = null;
        }
        recordCount = 0;
    }
}
