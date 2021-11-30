package io.castled.commons.streams;

import com.google.common.collect.Lists;
import io.castled.ObjectRegistry;
import io.castled.commons.models.FileFormat;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.filestorage.CastledS3Client;
import io.castled.schema.SchemaMapper;
import io.castled.schema.models.RecordSchema;
import io.castled.schema.models.Tuple;
import io.castled.utils.FileUtils;
import io.castled.utils.SizeUtils;
import io.castled.warehouses.connectors.redshift.models.S3PolledFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class S3FilesRecordInputStream implements RecordInputStream {

    private final List<S3PolledFile> s3PolledFiles;
    private final SchemaMapper schemaMapper;
    private final RecordSchema schema;
    private final FileRecordInputStreamFactory fileInputStreamFactory;
    private final Path unloadDirectory;
    private final int diskSpaceThresholdGBs;
    private final FileFormat fileFormat;
    private final CastledS3Client castledS3Client;
    private final boolean gzipped;

    private FileRecordInputStream currentInputStream;
    //index of s3polledFiles list
    private int bufferedTill = -1;

    private List<Path> bufferedFiles = Lists.newArrayList();
    //index of buffered files list
    private int readTill = -1;

    public S3FilesRecordInputStream(RecordSchema schema, SchemaMapper schemaMapper, List<S3PolledFile> s3PolledFiles,
                                    CastledS3Client castledS3Client, FileFormat fileFormat, Path unloadDirectory,
                                    int diskSpaceThresholdGBs, boolean gzipped) throws IOException {
        this.s3PolledFiles = s3PolledFiles;
        this.schemaMapper = schemaMapper;
        this.schema = schema;
        this.fileInputStreamFactory = ObjectRegistry.getInstance(FileRecordInputStreamFactory.class);
        this.unloadDirectory = unloadDirectory;
        this.diskSpaceThresholdGBs = diskSpaceThresholdGBs;
        this.castledS3Client = castledS3Client;
        this.fileFormat = fileFormat;
        FileUtils.deleteDirectory(unloadDirectory);
        Files.createDirectories(unloadDirectory);
        this.currentInputStream = new VoidRecordInputStream();
        this.gzipped = gzipped;
    }

    @Override
    public Tuple readRecord() {
        try {
            while (true) {
                Tuple record = this.currentInputStream.readRecord();
                if (record == null) {
                    if (this.currentInputStream.getFilePath() != null) {
                        Files.deleteIfExists(this.currentInputStream.getFilePath());
                    }
                    this.currentInputStream = refreshInputStream();
                    if (this.currentInputStream == null) {
                        return null;
                    }
                    continue;
                }
                return record;
            }
        } catch (Exception e) {
            log.error("S3 Record file stream failed", e);
            throw new CastledRuntimeException(e);
        }
    }

    private FileRecordInputStream refreshInputStream() throws IOException {
        if (readTill + 1 >= bufferedFiles.size()) {
            if (bufferedTill + 1 >= s3PolledFiles.size()) {
                return null;
            }
            this.bufferedFiles.clear();
            readTill = -1;
            List<String> objectsToDownload = getObjectsToDownload();
            this.bufferedFiles = castledS3Client.downloadFiles(objectsToDownload, unloadDirectory);
            this.bufferedTill = bufferedTill + objectsToDownload.size();
        }
        readTill++;
        return this.fileInputStreamFactory.getRecordInputStream(fileFormat,
                this.bufferedFiles.get(readTill), schema, schemaMapper, gzipped);
    }

    private List<String> getObjectsToDownload() {
        List<String> objectsToDownload = Lists.newArrayList();
        long totalSize = 0;
        for (int i = bufferedTill + 1; i < s3PolledFiles.size(); i++) {
            totalSize += s3PolledFiles.get(i).getContentLength();
            if (CollectionUtils.isEmpty(objectsToDownload)) {
                objectsToDownload.add(s3PolledFiles.get(i).getObjectKey());
            } else {
                if (totalSize > SizeUtils.convertGBToBytes(diskSpaceThresholdGBs)) {
                    break;
                }
                objectsToDownload.add(s3PolledFiles.get(i).getObjectKey());
            }
        }
        return objectsToDownload;
    }

}
