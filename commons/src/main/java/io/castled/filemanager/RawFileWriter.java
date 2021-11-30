package io.castled.filemanager;

import io.castled.exceptions.CastledRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
public class RawFileWriter implements AutoCloseable {

    private final long maxFileSizeBytes;
    private final Path fileDirectory;
    private final Supplier<String> fileNameSupplier;
    private final FileClosureListener fileClosureListener;

    private FileChannel fileChannel;
    private Path currentFile;
    private static final byte[] LINE_SEPARATOR = System.lineSeparator().getBytes();

    private long bytesWritten = 0;

    public RawFileWriter(long maxFileSizeBytes, Path fileDirectory, Supplier<String> fileNameSupplier) {
        this(maxFileSizeBytes, fileDirectory, fileNameSupplier, null);
    }

    public RawFileWriter(long maxFileSizeBytes, Path fileDirectory, Supplier<String> fileNameSupplier,
                         FileClosureListener fileClosureListener) {
        try {
            this.maxFileSizeBytes = maxFileSizeBytes;
            this.fileDirectory = fileDirectory;
            this.fileNameSupplier = fileNameSupplier;
            this.fileClosureListener = fileClosureListener;
            if (!Files.exists(fileDirectory)) {
                Files.createDirectories(fileDirectory);
            }
        } catch (IOException e) {
            throw new CastledRuntimeException(e);
        }
    }

    public void writeRecord(byte[] record) {
        try {
            if (fileChannel == null || !fileChannel.isOpen()) {
                String fileName = Optional.ofNullable(fileNameSupplier).map(Supplier::get).orElse(UUID.randomUUID().toString());
                this.currentFile = fileDirectory.resolve(fileName);
                fileChannel = new RandomAccessFile(this.currentFile.toFile(), "rw").getChannel();
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(record.length + LINE_SEPARATOR.length);
            byteBuffer.put(record);
            byteBuffer.put(LINE_SEPARATOR);
            ((Buffer) byteBuffer).flip();
            this.fileChannel.write(byteBuffer);
            if (bytesWritten > maxFileSizeBytes) {
                closeCurrentFile();
            }
            bytesWritten += bytesWritten + byteBuffer.position();

        } catch (IOException e) {
            log.error("Failed to write record {}", new String(record));
            throw new CastledRuntimeException(e);
        }
    }

    public void close() throws Exception {
        closeCurrentFile();
    }

    public void closeCurrentFile() throws IOException {
        this.fileChannel.force(false);
        this.fileChannel.close();
        if (fileClosureListener != null) {
            this.fileClosureListener.onFileClosure(this.currentFile);
        }
    }
}
