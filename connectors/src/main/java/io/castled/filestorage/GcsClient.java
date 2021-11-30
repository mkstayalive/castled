package io.castled.filestorage;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class GcsClient {

    @Getter
    private final Storage storage;

    private static final String SCHEME = "gs://";
    public static final String PATH_SEPARATOR = "/";

    public GcsClient(Storage storage) {
        this.storage = storage;
    }

    public static String constructGcsPath(String bucket, List<String> keyParts) {
        return String.format("%s%s%s", SCHEME, bucket + PATH_SEPARATOR, String.join(PATH_SEPARATOR, keyParts));
    }

    public static String constructObjectKey(List<String> keyParts) {
        return String.join(PATH_SEPARATOR, keyParts);
    }

    public List<Blob> listObjects(String bucket, String prefix) {
        Page<Blob> blobPage = storage.list(bucket, Storage.BlobListOption.prefix(prefix));
        return Streams.stream(blobPage.iterateAll()).collect(Collectors.toList());
    }

    public List<Path> downloadFiles(List<Blob> gcsFiles, Path downloadDirectory) throws IOException {


        List<Path> downloadedFiles = Lists.newArrayList();
        for (Blob gcsFile : gcsFiles) {

            Path file = downloadDirectory.resolve(UUID.randomUUID().toString());
            log.info("Download files started for files {}", gcsFiles.size());
            gcsFile.downloadTo(file);
            log.info("Download files finished for files {}", gcsFiles.size());

            downloadedFiles.add(file);
        }

        return downloadedFiles;
    }

    public List<String> uploadDirectory(String bucket, Path localDirectory, String gcsDirectory) throws IOException {
        List<String> uploadedFiles = Lists.newArrayList();
        for (Path path : Files.list(localDirectory).collect(Collectors.toList())) {
            String gcsFilePath = constructObjectKey(Lists.newArrayList(gcsDirectory, path.getFileName().toString()));
            uploadedFiles.add(constructGcsPath(bucket, Lists.newArrayList(gcsDirectory, path.getFileName().toString())));
            this.storage.create(BlobInfo.newBuilder(BlobId.of(bucket, gcsFilePath)).setContentType("text/plain").build(),
                    Files.readAllBytes(path));

        }
        return uploadedFiles;
    }

    public void deleteDirectory(String bucket, String gcsDirectory) {
        List<Blob> allBlobs = listObjects(bucket, gcsDirectory);
        for (Blob blob : allBlobs) {
            blob.delete();
        }
    }
}
