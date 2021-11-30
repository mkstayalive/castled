package io.castled.warehouses.connectors.snowflake;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.commons.models.FileFormat;
import io.castled.commons.models.FileStorageNamespace;
import io.castled.commons.streams.RecordInputStream;
import io.castled.commons.streams.S3FilesRecordInputStream;
import io.castled.constants.ConnectorExecutionConstants;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.filestorage.CastledS3Client;
import io.castled.schema.models.RecordSchema;
import io.castled.warehouses.S3BasedDataPoller;
import io.castled.warehouses.WarehouseConfig;
import io.castled.warehouses.connectors.redshift.models.S3PolledFile;
import io.castled.warehouses.models.WarehousePollContext;
import io.castled.warehouses.models.WarehousePollResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class SnowflakeDataPoller extends S3BasedDataPoller {

    private final SnowflakeClient snowflakeClient;
    private final SnowflakeResultSetSchemaMapper resultSetSchemaMapper;
    private final SnowflakeCsvSchemaMapper snowflakeCsvSchemaMapper;
    private final SnowflakeConnector snowflakeConnector;

    @Inject
    public SnowflakeDataPoller(SnowflakeClient snowflakeClient, SnowflakeConnector snowflakeConnector,
                               SnowflakeResultSetSchemaMapper resultSetSchemaMapper, SnowflakeCsvSchemaMapper snowflakeCsvSchemaMapper) {
        this.snowflakeClient = snowflakeClient;
        this.resultSetSchemaMapper = resultSetSchemaMapper;
        this.snowflakeCsvSchemaMapper = snowflakeCsvSchemaMapper;
        this.snowflakeConnector = snowflakeConnector;

    }

    @Override
    public WarehousePollResult pollRecords(WarehousePollContext warehousePollContext) {
        SnowflakeConnector snowflakeConnector = ObjectRegistry.getInstance(SnowflakeConnector.class);
        try {
            SnowflakeWarehouseConfig snowflakeWarehouseConfig = (SnowflakeWarehouseConfig) warehousePollContext.getWarehouseConfig();
            try (Connection connection = snowflakeConnector.getConnection(snowflakeWarehouseConfig)) {
                List<String> bookKeepingTables = snowflakeClient.listTables(connection, ConnectorExecutionConstants.CASTLED_CONTAINER.toUpperCase());
                createUncommittedSnapshot(connection, warehousePollContext, bookKeepingTables);
                recoverSnapshotFromBackup(connection, bookKeepingTables, warehousePollContext);
                RecordSchema querySchema = getSchemaFromQuery(connection, String.format("select * from %s",
                        ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID())));
                return WarehousePollResult.builder()
                        .recordInputStream(createRecordStream(connection, warehousePollContext, bookKeepingTables, querySchema))
                        .warehouseSchema(querySchema)
                        .build();
            }

        } catch (Exception e) {
            log.error("Snowflake data poll failed for pipeline {}", warehousePollContext.getPipelineUUID());
            throw new CastledRuntimeException(e);
        }
    }

    @Override
    public WarehousePollResult resumePoll(WarehousePollContext warehousePollContext) {
        try {
            SnowflakeWarehouseConfig snowflakeWarehouseConfig = (SnowflakeWarehouseConfig) warehousePollContext.getWarehouseConfig();
            CastledS3Client s3Client = SnowflakeUtils.getS3Client(warehousePollContext.getWarehouseConfig(), warehousePollContext.getDataEncryptionKey());
            String unloadDirectoryKey = getS3UnloadDirectory(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId());

            List<S3PolledFile> s3PolledFiles = s3Client.listObjects(unloadDirectoryKey).stream().map(this::buildS3PolledFile).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(s3PolledFiles)) {
                return pollRecords(warehousePollContext);
            }
            RecordSchema querySchema = getQuerySchema(snowflakeWarehouseConfig, String.format("select * from %s",
                    ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID())));

            S3FilesRecordInputStream s3FilesRecordInputStream = new S3FilesRecordInputStream(querySchema, snowflakeCsvSchemaMapper, s3PolledFiles, s3Client, FileFormat.CSV,
                    getPipelineRunUnloadDirectory(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId()),
                    20, true);

            return WarehousePollResult.builder().warehouseSchema(querySchema).recordInputStream(s3FilesRecordInputStream).resumed(true).build();
        } catch (Exception e) {
            log.error("Snowflake data poll resume failed for pipeline {}", warehousePollContext.getPipelineUUID());
            return pollRecords(warehousePollContext);
        }
    }

    private RecordSchema getQuerySchema(SnowflakeWarehouseConfig snowflakeWarehouseConfig, String query) throws SQLException {
        try (Connection connection = snowflakeConnector.getConnection(snowflakeWarehouseConfig)) {
            return getSchemaFromQuery(connection, query);
        }

    }

    @Override
    public void cleanupPipelineResources(String pipelineUUID, WarehouseConfig warehouseConfig) {
        try {

            SnowflakeConnector snowflakeConnector = ObjectRegistry.getInstance(SnowflakeConnector.class);
            try (Connection connection = snowflakeConnector.getConnection((SnowflakeWarehouseConfig) warehouseConfig)) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(String.format("drop table if exists %s", ConnectorExecutionConstants.getQualifiedCommittedSnapshot(pipelineUUID)));
                    statement.execute(String.format("drop table if exists %s", ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(pipelineUUID)));

                }
            }
        } catch (SQLException e) {
            log.error("Cleanup pipeline resources failed for pipeline {}", pipelineUUID);
            throw new CastledRuntimeException(e);
        }
    }


    private void recoverSnapshotFromBackup(Connection connection, List<String> bookKeepingTables,
                                           WarehousePollContext warehousePollRequest) throws SQLException {
        String qualifiedCommittedSnapshotBkp = ConnectorExecutionConstants.getQualifiedCommittedSnapshotBkp(warehousePollRequest.getPipelineUUID());
        if (bookKeepingTables.contains(ConnectorExecutionConstants.getCommittedSnapshotBackup(warehousePollRequest.getPipelineUUID()).toUpperCase())) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("alter table %s rename to %s", qualifiedCommittedSnapshotBkp,
                        ConnectorExecutionConstants.getCommittedSnapshot(warehousePollRequest.getPipelineUUID())));
            }
        }
    }

    private void createUncommittedSnapshot(Connection connection, WarehousePollContext warehousePollRequest,
                                           List<String> internalTables) throws SQLException {

        if (internalTables.contains(ConnectorExecutionConstants.getUncommittedSnapshot(warehousePollRequest.getPipelineUUID()).toUpperCase())) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("drop table if exists %s",
                        ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollRequest.getPipelineUUID())));
            }
        }
        this.snowflakeClient.createTableFromQuery(connection, ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollRequest.getPipelineUUID()),
                warehousePollRequest.getQuery(), false);
    }

    private void createInternalSchemaIfRequired(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format("create schema if not exists %s", ConnectorExecutionConstants.CASTLED_CONTAINER));
        }
    }

    private RecordInputStream createRecordStream(Connection connection, WarehousePollContext warehousePollContext,
                                                 List<String> bookKeepingTables, RecordSchema querySchema) throws SQLException, IOException {

        SnowflakeWarehouseConfig snowflakeWarehouseConfig = (SnowflakeWarehouseConfig) warehousePollContext.getWarehouseConfig();
        CastledS3Client s3Client = SnowflakeUtils.getS3Client(warehousePollContext.getWarehouseConfig(), warehousePollContext.getDataEncryptionKey());
        String bucket = s3Client.getBucket();
        String unloadDirectory = CastledS3Client.constructS3Path(bucket,
                Lists.newArrayList(FileStorageNamespace.PIPELINE_UNLOADS.getNamespace(), warehousePollContext.getPipelineUUID(),
                        String.valueOf(warehousePollContext.getPipelineRunId())));

        String unloadQuery = String.format("COPY INTO '%s/' FROM (%s) " +
                        "FILE_FORMAT = (TYPE = 'CSV' COMPRESSION = 'GZIP' FIELD_OPTIONALLY_ENCLOSED_BY = '\"' NULL_IF = ('NULL', 'null') EMPTY_FIELD_AS_NULL=FALSE " +
                        "DATE_FORMAT = 'YYYY-MM-DD' TIMESTAMP_FORMAT = 'YYYY-MM-DD HH24:MI:SS.FF3 TZHTZM' TIME_FORMAT = 'HH24:MI:SS')" +
                        "CREDENTIALS = (AWS_KEY_ID = '%s' AWS_SECRET_KEY = '%s') " +
                        "ENCRYPTION = (TYPE = 'AWS_CSE'  MASTER_KEY = '%s' ) OVERWRITE=TRUE HEADER=TRUE",
                unloadDirectory, getDataFetchQuery(warehousePollContext, bookKeepingTables), snowflakeWarehouseConfig.getAccessKeyId(),
                snowflakeWarehouseConfig.getAccessKeySecret(), s3Client.getEncryptionKey());

        try (Statement statement = connection.createStatement()) {
            statement.execute(unloadQuery);
        }

        String unloadDirectoryKey = getS3UnloadDirectory(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId());

        List<S3PolledFile> s3PolledFiles = s3Client.listObjects(unloadDirectoryKey).stream().map(this::buildS3PolledFile).collect(Collectors.toList());

        return new S3FilesRecordInputStream(querySchema, snowflakeCsvSchemaMapper, s3PolledFiles, s3Client, FileFormat.CSV,
                getPipelineRunUnloadDirectory(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId()),
                20, true);
    }

    private S3PolledFile buildS3PolledFile(S3ObjectSummary s3ObjectSummary) {
        return new S3PolledFile(s3ObjectSummary.getBucketName(), s3ObjectSummary.getKey(), s3ObjectSummary.getSize());

    }

    private String getDataFetchQuery(WarehousePollContext warehousePollContext, List<String> bookKeepingTables) {
        String committedSnapshot = ConnectorExecutionConstants.getQualifiedCommittedSnapshot(warehousePollContext.getPipelineUUID());
        String uncommittedSnapshot = ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID());
        if (bookKeepingTables.contains(ConnectorExecutionConstants.getCommittedSnapshot(warehousePollContext.getPipelineUUID()).toUpperCase())) {
            return String.format("select * from %s except select * from %s", uncommittedSnapshot, committedSnapshot);
        }
        return String.format("select * from %s", uncommittedSnapshot);

    }

    private RecordSchema getSchemaFromQuery(Connection connection, String query) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            return resultSetSchemaMapper.getSchema(preparedStatement.getMetaData());
        }
    }

    @Override
    public CastledS3Client getS3Client(WarehouseConfig warehouseConfig, String encryptionKey) {
        return SnowflakeUtils.getS3Client(warehouseConfig, encryptionKey);
    }
}
