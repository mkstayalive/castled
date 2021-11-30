package io.castled.filestorage;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.google.common.collect.Lists;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CastledS3Client implements ObjectStoreClient {

    private final AmazonS3 s3Client;
    private final TransferManager transferManager;
    @Getter
    private final String bucket;

    @Getter
    private final Regions region;

    @Getter
    private final String encryptionKey;

    private static final String SCHEME = "s3://";
    public static final String PATH_SEPARATOR = "/";

    public CastledS3Client(AWSCredentialsProvider awsCredentialsProvider, String encryptionKey,
                           Regions region, String bucket) {
        this.s3Client = getAmazonS3(awsCredentialsProvider, encryptionKey, region);
        this.transferManager = TransferManagerBuilder.standard().withS3Client(this.s3Client).build();
        this.bucket = bucket;
        this.encryptionKey = encryptionKey;
        this.region = region;
    }

    public CastledS3Client(String accessKeyId, String accessKeySecret, String encryptionKey,
                           Regions region, String bucket) {
        this(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, accessKeySecret)),
                encryptionKey, region, bucket);

    }

    private AmazonS3 getAmazonS3(AWSCredentialsProvider awsCredentialsProvider, String encryptionKey,
                                 Regions region) {
        if (encryptionKey == null) {
            return AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(region).build();
        }
        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(encryptionKey),
                "AES");
        return AmazonS3EncryptionClientBuilder.standard().withCredentials(awsCredentialsProvider)
                .withRegion(region)
                .withCryptoConfiguration(new CryptoConfiguration(CryptoMode.EncryptionOnly))
                .withEncryptionMaterials(new StaticEncryptionMaterialsProvider(new EncryptionMaterials(secretKey))).build();
    }

    public void uploadFile(String key, File file) throws ObjectStoreException {
        try {
            this.s3Client.putObject(this.bucket, key, file);
        } catch (Exception e) {
            log.error("Upload file failed for bucket {} and key {}", this.bucket, key);
            throw new ObjectStoreException(e.getMessage(), e);
        }
    }

    public void uploadText(String key, String objectAsString) throws ObjectStoreException {
        try {
            ObjectMetadata metaData = new ObjectMetadata();
            byte[] contentBytes = objectAsString.getBytes();
            metaData.setContentLength(contentBytes.length);

            InputStream is = new ByteArrayInputStream(contentBytes);
            s3Client.putObject(new PutObjectRequest(this.getBucket(), key, is, metaData));
        } catch (Exception e) {
            log.error("Upload text failed for bucket {} and key {}", this.bucket, key);
            throw new ObjectStoreException(e.getMessage(), e);
        }
    }

    public void uploadDirectory(Path directory, String s3DirectoryPrefix) {
        try {
            MultipleFileUpload multipleFileUpload = this.transferManager.uploadDirectory(bucket, s3DirectoryPrefix, directory.toFile(), true);
            multipleFileUpload.waitForCompletion();
        } catch (Exception e) {
            log.error("Directory {} upload failed to s3", directory.toString());
            throw new CastledRuntimeException(e);
        }
    }

    public String getObjectAsString(String key) {
        try {
            return this.s3Client.getObjectAsString(bucket, key);
        } catch (AmazonS3Exception e) {
            if (e.getErrorCode().equals("NoSuchKey")) {
                return null;
            }
            throw e;
        }
    }

    public static String constructS3Path(String bucket, List<String> keyParts) {
        return String.format("%s%s%s", SCHEME, bucket + PATH_SEPARATOR, String.join(PATH_SEPARATOR, keyParts));
    }

    public static String constructObjectKey(List<String> keyParts) {
        return String.join(PATH_SEPARATOR, keyParts);
    }


    public static ImmutablePair<String, String> getBucketAndKey(String url) {
        String[] schemeAndRest = url.split("://");
        if (schemeAndRest.length != 2) {
            throw new CastledRuntimeException("Invalid s3 url: " + url);
        }
        String[] urlParts = schemeAndRest[1].split(PATH_SEPARATOR);
        List<String> keyParts = Lists.newArrayList(Arrays.asList(urlParts).subList(1, urlParts.length));
        return new ImmutablePair<>(urlParts[0], String.join(PATH_SEPARATOR, keyParts));
    }

    public static String getFileName(String objectKey) {
        String[] keyParts = objectKey.split(PATH_SEPARATOR);
        if (keyParts.length == 0) {
            throw new CastledRuntimeException("Invalid object key " + objectKey);
        }
        return keyParts[keyParts.length - 1];
    }


    public List<Path> downloadFiles(List<String> objectKeys, Path downloadDir) {
        try {
            List<Download> submittedDownloads = Lists.newArrayList();
            List<Path> localFiles = Lists.newArrayList();
            for (String objectKey : objectKeys) {
                String fileName = getFileName(objectKey);
                localFiles.add(downloadDir.resolve(fileName));
                submittedDownloads.add(this.transferManager.download(bucket, objectKey, downloadDir.resolve(fileName).toFile()));
            }
            for (Download submittedDownload : submittedDownloads) {
                submittedDownload.waitForCompletion();
            }
            return localFiles;
        } catch (Exception e) {
            log.error("Download files {} failed", JsonUtils.objectToString(objectKeys), e);
            throw new CastledRuntimeException(e);
        }
    }

    public List<String> listObjectUrls(String directoryKey) {
        return listObjects(directoryKey).stream().map(s3ObjectSummary ->
                constructS3Path(s3ObjectSummary.getBucketName(), Lists.newArrayList(s3ObjectSummary.getKey())))
                .collect(Collectors.toList());
    }

    public List<S3ObjectSummary> listObjects(String directoryKey) {
        List<S3ObjectSummary> s3ObjectSummaries = Lists.newArrayList();
        ObjectListing objectListing = this.s3Client.listObjects(new ListObjectsRequest().withBucketName(bucket)
                .withPrefix(directoryKey));
        s3ObjectSummaries.addAll(objectListing.getObjectSummaries());
        while (objectListing.isTruncated()) {
            objectListing = this.s3Client.listNextBatchOfObjects(objectListing);
            s3ObjectSummaries.addAll(objectListing.getObjectSummaries());
        }
        return s3ObjectSummaries;
    }

    public void deleteDirectory(String directoryKey) {
        List<S3ObjectSummary> objectSummaries = listObjects(directoryKey);
        if (CollectionUtils.isNotEmpty(objectSummaries)) {
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket)
                    .withKeys(objectSummaries.stream().map(S3ObjectSummary::getKey).toArray(String[]::new));
            this.s3Client.deleteObjects(deleteObjectsRequest);
        }
    }
}
