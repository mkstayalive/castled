package io.castled.warehouses.connectors.snowflake;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.castled.ObjectRegistry;
import io.castled.constants.ConnectorExecutionConstants;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.filemanager.CsvFileWriter;
import io.castled.filestorage.CastledS3Client;
import io.castled.schema.models.FieldSchema;
import io.castled.schema.models.Tuple;
import io.castled.utils.CastledExceptionUtils;
import io.castled.utils.FileUtils;
import io.castled.utils.SizeUtils;
import io.castled.warehouses.S3BasedWarehouseSyncFailureListener;
import io.castled.warehouses.WarehouseConfig;
import io.castled.warehouses.WarehouseConnectorConfig;
import io.castled.warehouses.models.WarehousePollContext;
import lombok.extern.slf4j.Slf4j;
import net.snowflake.client.jdbc.SnowflakeSQLException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class SnowflakeSyncFailureListener extends S3BasedWarehouseSyncFailureListener {

    private final SnowflakeConnector snowflakeConnector;
    private final CsvFileWriter csvFileWriter;
    private final WarehouseConfig warehouseConfig;
    private final WarehousePollContext warehousePollContext;
    private final String s3UploadDir;

    private long pendingRecords = 0;
    private long failedRecords = 0;


    public SnowflakeSyncFailureListener(WarehousePollContext warehousePollContext) throws IOException {
        super(warehousePollContext, SnowflakeUtils.getS3Client(warehousePollContext.getWarehouseConfig(), warehousePollContext.getDataEncryptionKey()));
        this.warehousePollContext = warehousePollContext;
        this.warehouseConfig = warehousePollContext.getWarehouseConfig();
        this.snowflakeConnector = ObjectRegistry.getInstance(SnowflakeConnector.class);
        this.csvFileWriter = new CsvFileWriter(50000, failureRecordsDirectory,
                () -> UUID.randomUUID().toString(), trackableFields);
        this.s3UploadDir = getS3FailedRecordsDirectory(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId());
    }

    @Override
    public void doFlush() throws Exception {
        if (pendingRecords > 0) {
            this.csvFileWriter.close();
            uploadFilesToS3();
        }
        SnowflakeWarehouseConfig snowflakeWarehouseConfig = (SnowflakeWarehouseConfig) warehouseConfig;
        try (Connection connection = this.snowflakeConnector.getConnection(snowflakeWarehouseConfig)) {
            if (failedRecords > 0) {
                copyFailedRecords(connection, snowflakeWarehouseConfig);
                removeFailedRecordsFromSnapshot(connection);
            }
            commitSnapshot(connection);
        }
    }

    private void copyFailedRecords(Connection connection,
                                   SnowflakeWarehouseConfig snowflakeWarehouseConfig) throws SQLException {
        String failedRecordTable = createFailedRecordsTable(connection);
        ObjectRegistry.getInstance(SnowflakeClient.class).copyFilesToTable(connection,
                failedRecordTable, CastledS3Client.constructS3Path(castledS3Client.getBucket(), Lists.newArrayList(this.s3UploadDir)), castledS3Client.getEncryptionKey(),
                snowflakeWarehouseConfig.getAccessKeyId(), snowflakeWarehouseConfig.getAccessKeySecret());
        castledS3Client.deleteDirectory(getS3FailedRecordsDirectory(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId()));
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

    private void commitSnapshot(Connection connection) {
        String uncommittedSnapshot = ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID());
        String committedSnapshotBackup = ConnectorExecutionConstants.getQualifiedCommittedSnapshotBkp(warehousePollContext.getPipelineUUID());
        try {
            backupSnapshotTable(connection);
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("alter table %s rename to %s", uncommittedSnapshot,
                        ConnectorExecutionConstants.getQualifiedCommittedSnapshot(warehousePollContext.getPipelineUUID())));
                statement.execute(String.format("drop table if exists %s", committedSnapshotBackup));
            }
        } catch (Exception e) {
            log.error("Committing snapshot for pipeline {} failed", warehousePollContext.getPipelineUUID(), e);
            throw new CastledRuntimeException(e);
        }
    }

    private void backupSnapshotTable(Connection connection) throws SQLException {
        String committedSnapshot = ConnectorExecutionConstants.getQualifiedCommittedSnapshot(warehousePollContext.getPipelineUUID());
        try {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("alter table %s rename to %s", committedSnapshot,
                        ConnectorExecutionConstants.getQualifiedCommittedSnapshotBkp(warehousePollContext.getPipelineUUID())));
            }
        } catch (SnowflakeSQLException e) {
            if (!CastledExceptionUtils.hasMessage(e, "does not exist or not authorized")) {
                throw e;
            }
        }
    }

    @Override
    public synchronized void doWriteRecord(Tuple record) throws Exception {
        List<Object> copyableValues = getCopyableValues(record);
        this.csvFileWriter.writeRecord(copyableValues);
        failedRecords++;
        pendingRecords++;
        if (pendingRecords > SizeUtils.convertGBToBytes(ObjectRegistry.getInstance(WarehouseConnectorConfig.class).getFailedMaxRecordsCount())) {
            this.csvFileWriter.close();
            uploadFilesToS3();
            if (!Files.exists(failureRecordsDirectory)) {
                Files.createDirectory(failureRecordsDirectory);
            }
        }
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

    private void uploadFilesToS3() throws IOException {

        FileUtils.listFiles(failureRecordsDirectory).forEach(this::compressFile);
        castledS3Client.uploadDirectory(failureRecordsDirectory, this.s3UploadDir);
        FileUtils.deleteDirectory(failureRecordsDirectory);
        pendingRecords = 0;
    }

    private List<Object> getCopyableValues(Tuple record) throws Exception {
        SnowflakeCopySchemaAdapter copySchemaAdapter = ObjectRegistry.getInstance(SnowflakeCopySchemaAdapter.class);
        Map<String, Object> copyableValues = Maps.newHashMap();
        for (FieldSchema fieldSchema : warehousePollContext.getWarehouseSchema().getFieldSchemas()) {
            if (trackableFields.contains(fieldSchema.getName())) {
                copyableValues.put(fieldSchema.getName(), copySchemaAdapter.transformValue(record.getValue(fieldSchema.getName()),
                        fieldSchema.getSchema()));
            }
        }
        return trackableFields.stream().map(copyableValues::get).collect(Collectors.toList());
    }


    private String createFailedRecordsTable(Connection connection) throws SQLException {

        SnowflakeClient snowflakeClient = ObjectRegistry.getInstance(SnowflakeClient.class);
        String failedRecordsCreateQuery = String.format("select %s from %s limit 0",
                String.join(",", trackableFields), ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID()));
        String tableName = ConnectorExecutionConstants.getFailedRecordsTable(warehousePollContext.getPipelineUUID());
        snowflakeClient.createTableFromQuery(connection, tableName, failedRecordsCreateQuery, true);
        return tableName;
    }

}
