package io.castled.warehouses.connectors.redshift;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.castled.ObjectRegistry;
import io.castled.constants.ConnectorExecutionConstants;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.filemanager.RawFileWriter;
import io.castled.filestorage.CastledS3Client;
import io.castled.filestorage.ObjectStoreException;
import io.castled.schema.models.FieldSchema;
import io.castled.schema.models.Tuple;
import io.castled.utils.FileUtils;
import io.castled.utils.JsonUtils;
import io.castled.utils.SizeUtils;
import io.castled.warehouses.S3BasedWarehouseSyncFailureListener;
import io.castled.warehouses.WarehouseConnectorConfig;
import io.castled.warehouses.connectors.redshift.models.RedshiftS3CopyManifest;
import io.castled.warehouses.models.WarehousePollContext;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class RedshiftSyncFailureListener extends S3BasedWarehouseSyncFailureListener {

    private final RedshiftConnector redshiftConnector;
    private final RawFileWriter rawFileWriter;
    private final RedshiftWarehouseConfig warehouseConfig;
    private final WarehousePollContext warehousePollContext;
    private final String s3UploadDir;
    private final CastledS3Client encryptedS3Client;
    private final CastledS3Client simpleS3Client;

    private int totalBytes = 0;
    private long failedRecords = 0;

    public RedshiftSyncFailureListener(WarehousePollContext warehousePollContext) {
        super(warehousePollContext, RedshiftUtils.getS3Client(warehousePollContext.getWarehouseConfig(),
                warehousePollContext.getDataEncryptionKey()));
        this.warehouseConfig = (RedshiftWarehouseConfig) warehousePollContext.getWarehouseConfig();
        this.redshiftConnector = ObjectRegistry.getInstance(RedshiftConnector.class);
        this.rawFileWriter = new RawFileWriter(SizeUtils.convertMBToBytes(50), failureRecordsDirectory,
                () -> UUID.randomUUID().toString());
        this.warehousePollContext = warehousePollContext;
        this.encryptedS3Client = RedshiftUtils.getS3Client(warehouseConfig, warehousePollContext.getDataEncryptionKey());
        this.simpleS3Client = RedshiftUtils.getS3Client(warehouseConfig, null);
        this.s3UploadDir = getS3FailedRecordsDirectory(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId());
    }

    @Override
    public synchronized void doWriteRecord(Tuple tuple) throws Exception {
        String record = getCopyableRecord(tuple);
        byte[] recordBytes = record.getBytes();
        this.rawFileWriter.writeRecord(recordBytes);
        totalBytes += recordBytes.length;
        failedRecords++;
        if (totalBytes > SizeUtils.convertGBToBytes(ObjectRegistry.getInstance(WarehouseConnectorConfig.class).getFailedRecordBufferSize())) {
            this.rawFileWriter.close();
            uploadFilesToS3();
            if (!Files.exists(failureRecordsDirectory)) {
                Files.createDirectory(failureRecordsDirectory);
            }
        }
    }

    private String getCopyableRecord(Tuple record) throws Exception {
        RedshiftCopySchemaMapper copySchemaMapper = ObjectRegistry.getInstance(RedshiftCopySchemaMapper.class);
        Map<String, Object> copyableValues = Maps.newHashMap();
        for (FieldSchema field : warehousePollContext.getWarehouseSchema().getFieldSchemas()) {
            if (trackableFields.contains(field.getName())) {
                copyableValues.put(field.getName(), copySchemaMapper.transformValue(record.getValue(field.getName()), field.getSchema()));
            }
        }
        return JsonUtils.objectToString(copyableValues);
    }

    @Override
    public void doFlush() throws Exception {
        if (totalBytes > 0) {
            this.rawFileWriter.close();
            uploadFilesToS3();
        }
        try (Connection connection = this.redshiftConnector.getConnection(warehouseConfig)) {
            if (failedRecords > 0) {
                copyFailedRecords(connection);
                removeFailedRecordsFromSnapshot(connection);
            }
            commitSnapshot(connection);
        }

    }

    private void commitSnapshot(Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        String uncommittedSnapshot = ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID());
        String committedSnapshot = ConnectorExecutionConstants.getQualifiedCommittedSnapshot(warehousePollContext.getPipelineUUID());
        try {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("drop table if exists %s", committedSnapshot));
                statement.execute(String.format("alter table %s rename to %s", uncommittedSnapshot,
                        ConnectorExecutionConstants.getCommittedSnapshot(warehousePollContext.getPipelineUUID())));
            }
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            log.error("Committing snapshot for pipeline {} failed", warehousePollContext.getPipelineUUID(), e);
            throw new CastledRuntimeException(e);
        }
        connection.setAutoCommit(true);

    }

    private void removeFailedRecordsFromSnapshot(Connection connection) throws SQLException {

        String uncommittedSnapshot = ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID());
        String failedRecordsTable = ConnectorExecutionConstants.getFailedRecordsTable(warehousePollContext.getPipelineUUID());
        StringBuilder failedRecordsDeleteQuery = new StringBuilder(String.format("delete from %s using %s where 1 = 1", uncommittedSnapshot, failedRecordsTable));
        for (String trackableField : trackableFields) {
            failedRecordsDeleteQuery.append(String.format(" AND (%s.%s = %s.%s OR (%s.%s IS NULL and %s.%s IS NULL))",
                    failedRecordsTable, trackableField, uncommittedSnapshot, trackableField, failedRecordsTable, trackableField, uncommittedSnapshot, trackableField));
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(failedRecordsDeleteQuery.toString());
        }

    }

    private void uploadFilesToS3() throws IOException {

        FileUtils.listFiles(failureRecordsDirectory).forEach(this::compressFile);
        encryptedS3Client.uploadDirectory(failureRecordsDirectory, this.s3UploadDir);
        FileUtils.deleteDirectory(failureRecordsDirectory);
        totalBytes = 0;
    }

    private void compressFile(Path inputFile) {
        try {
            String compressedFile = inputFile.toString() + ".gzip";
            FileUtils.compressFile(inputFile, Paths.get(compressedFile));
            Files.deleteIfExists(inputFile);
        } catch (Exception e) {
            log.error("File compressed failed for file {}", inputFile.toString());
            throw new CastledRuntimeException(e);
        }
    }

    private void copyFailedRecords(Connection connection) throws SQLException, ObjectStoreException {
        createFailedRecordsTable(connection);
        RedshiftS3CopyManifest redshiftS3CopyManifest = new RedshiftS3CopyManifest(encryptedS3Client.listObjectUrls(this.s3UploadDir)
                .stream().map(s3Url -> new RedshiftS3CopyManifest.ManifestEntry(s3Url, true))
                .collect(Collectors.toList()));
        this.simpleS3Client.uploadText(CastledS3Client.constructObjectKey(Lists.newArrayList(this.s3UploadDir, "manifest.json")),
                JsonUtils.objectToString(redshiftS3CopyManifest));
        String manifestFileUrl = CastledS3Client.constructS3Path(encryptedS3Client.getBucket(), Lists.newArrayList(this.s3UploadDir, "manifest.json"));
        ObjectRegistry.getInstance(RedshiftClient.class).copyFilesToTable(connection,
                ConnectorExecutionConstants.getFailedRecordsTable(warehousePollContext.getPipelineUUID()),
                manifestFileUrl, encryptedS3Client.getEncryptionKey(), warehouseConfig);
        encryptedS3Client.deleteDirectory(getS3FailedRecordsDirectory(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId()));

    }

    private void createFailedRecordsTable(Connection connection) throws SQLException {

        RedshiftClient redshiftClient = ObjectRegistry.getInstance(RedshiftClient.class);
        String failedRecordsCreateQuery = String.format("select %s from %s limit 0",
                String.join(",", trackableFields), ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID()));
        String tableName = ConnectorExecutionConstants.getFailedRecordsTable(warehousePollContext.getPipelineUUID());
        RedshiftTableProperties redshiftTableProperties = (RedshiftTableProperties)
                this.redshiftConnector.getSnapshotTableProperties(warehousePollContext.getPrimaryKeys());
        redshiftClient.createTableFromQuery(connection, tableName, failedRecordsCreateQuery, redshiftTableProperties, true);
    }
}
