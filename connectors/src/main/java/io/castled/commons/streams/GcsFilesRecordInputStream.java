package io.castled.commons.streams;

import com.google.cloud.storage.Blob;
import com.google.common.collect.Lists;
import io.castled.ObjectRegistry;
import io.castled.commons.models.FileFormat;
import io.castled.exceptions.CastledRuntimeException;

import io.castled.filestorage.GcsClient;
import io.castled.schema.SchemaMapper;
import io.castled.schema.models.Tuple;
import io.castled.schema.models.RecordSchema;
import io.castled.utils.SizeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class GcsFilesRecordInputStream implements RecordInputStream {

    private final List<Blob> gcsBlobs;
    private final SchemaMapper schemaMapper;
    private final RecordSchema schema;
    private final FileRecordInputStreamFactory fileInputStreamFactory;
    private final Path unloadDirectory;
    private final int diskSpaceThresholdGBs;
    private final FileFormat fileFormat;
    private final GcsClient gcsClient;
    private final boolean gzipped;

    private FileRecordInputStream currentInputStream;
    //index of s3polledFiles list
    private int bufferedTill = -1;

    private List<Path> bufferedFiles = Lists.newArrayList();
    //index of buffered files list
    private int readTill = -1;

    public GcsFilesRecordInputStream(RecordSchema schema, SchemaMapper schemaMapper, List<Blob> gcsBlobs,
                                     FileFormat fileFormat, Path unloadDirectory, GcsClient gcsClient,
                                     int diskSpaceThresholdGBs, boolean gzipped) throws IOException {

        this.schemaMapper = schemaMapper;
        this.schema = schema;
        this.fileInputStreamFactory = ObjectRegistry.getInstance(FileRecordInputStreamFactory.class);
        this.unloadDirectory = unloadDirectory;
        this.diskSpaceThresholdGBs = diskSpaceThresholdGBs;
        this.gcsClient = gcsClient;
        this.fileFormat = fileFormat;
        this.gcsBlobs = gcsBlobs;
        if (!Files.exists(unloadDirectory)) {
            Files.createDirectories(unloadDirectory);
        }
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
            log.error("Gcs record file stream parsing failed", e);
            throw new CastledRuntimeException(e);
        }
    }

    private FileRecordInputStream refreshInputStream() throws IOException {
        if (readTill + 1 >= bufferedFiles.size()) {
            if (bufferedTill + 1 >= gcsBlobs.size()) {
                return null;
            }
            this.bufferedFiles.clear();
            readTill = -1;
            List<Blob> objectsToDownload = getObjectsToDownload();
            this.bufferedFiles = gcsClient.downloadFiles(objectsToDownload, unloadDirectory);
            this.bufferedTill = bufferedTill + objectsToDownload.size();
        }
        readTill++;
        return this.fileInputStreamFactory.getRecordInputStream(fileFormat,
                this.bufferedFiles.get(readTill), schema, schemaMapper, gzipped);
    }

    private List<Blob> getObjectsToDownload() {
        List<Blob> objectsToDownload = Lists.newArrayList();
        long totalSize = 0;
        for (int i = bufferedTill + 1; i < gcsBlobs.size(); i++) {
            totalSize += gcsBlobs.get(i).getSize();
            if (CollectionUtils.isEmpty(objectsToDownload)) {
                objectsToDownload.add(gcsBlobs.get(i));
            } else {
                if (totalSize > SizeUtils.convertGBToBytes(diskSpaceThresholdGBs)) {
                    break;
                }
                objectsToDownload.add(gcsBlobs.get(i));
            }
        }
        return objectsToDownload;
    }
}