package io.castled.warehouses.connectors.redshift;

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
import io.castled.utils.JsonUtils;
import io.castled.warehouses.S3BasedDataPoller;
import io.castled.warehouses.WarehouseConfig;
import io.castled.warehouses.WarehouseConnectorConfig;
import io.castled.warehouses.connectors.redshift.models.RedshiftS3UnloadManifest;
import io.castled.warehouses.connectors.redshift.models.S3PolledFile;
import io.castled.warehouses.models.WarehousePollContext;
import io.castled.warehouses.models.WarehousePollResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class RedshiftDataPoller extends S3BasedDataPoller {

    private final RedshiftClient redshiftClient;
    private final WarehouseConnectorConfig warehouseConnectorConfig;
    private final RedshiftResultSetSchemaMapper resultSetSchemaMapper;

    @Inject
    public RedshiftDataPoller(RedshiftClient redshiftClient,
                              WarehouseConnectorConfig warehouseConnectorConfig,
                              RedshiftResultSetSchemaMapper resultSetSchemaMapper) {
        this.redshiftClient = redshiftClient;
        this.warehouseConnectorConfig = warehouseConnectorConfig;
        this.resultSetSchemaMapper = resultSetSchemaMapper;
    }

    private static S3PolledFile s3PolledFile(RedshiftS3UnloadManifest.ManifestEntry manifestEntry) {
        ImmutablePair<String, String> bucketAndKey = CastledS3Client.getBucketAndKey(manifestEntry.getUrl());
        return new S3PolledFile(bucketAndKey.getKey(), bucketAndKey.getValue(), manifestEntry.getMeta().getContentLength());
    }

    @Override
    public WarehousePollResult pollRecords(WarehousePollContext warehousePollContext) {
        RedshiftConnector redshiftConnector = ObjectRegistry.getInstance(RedshiftConnector.class);
        try {
            RedshiftWarehouseConfig redshiftWarehouseConfig = (RedshiftWarehouseConfig) warehousePollContext.getWarehouseConfig();
            try (Connection connection = redshiftConnector.getConnection(redshiftWarehouseConfig)) {
                List<String> bookKeepingTables = this.redshiftClient.listTables(connection, ConnectorExecutionConstants.CASTLED_CONTAINER);
                createUncommittedSnapshot(connection, warehousePollContext, bookKeepingTables);
                RecordSchema querySchema = getSchemaFromQuery(connection, String.format("select * from %s",
                        ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID())));
                return WarehousePollResult.builder()
                        .recordInputStream(createRecordStream(connection, warehousePollContext, bookKeepingTables, querySchema))
                        .warehouseSchema(querySchema).build();

            }
        } catch (Exception e) {
            log.error("Poll records from warehouse {} failed", warehousePollContext.getWarehouseConfig().getType(), e);
            throw new CastledRuntimeException(e);
        }
    }

    @Override
    public WarehousePollResult resumePoll(WarehousePollContext warehousePollContext) {

        try {
            RedshiftWarehouseConfig warehouseConfig = (RedshiftWarehouseConfig) warehousePollContext.getWarehouseConfig();
            CastledS3Client s3Client = new CastledS3Client(warehouseConfig.getAccessKeyId(),
                    warehouseConfig.getAccessKeySecret(), warehousePollContext.getDataEncryptionKey(), warehouseConfig.getRegion(), warehouseConfig.getS3Bucket());
            String unloadDirectoryKey = getS3UnloadDirectory(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId());
            String manifestFilePath = unloadDirectoryKey + CastledS3Client.PATH_SEPARATOR + "manifest";
            String manifest = s3Client.getObjectAsString(manifestFilePath);
            if (manifest == null) {
                return pollRecords(warehousePollContext);
            }

            RecordSchema querySchema = getQuerySchema(warehouseConfig, String.format("select * from %s",
                    ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID())));

            RedshiftS3UnloadManifest polledFilesManifest = JsonUtils.jsonStringToObject(manifest, RedshiftS3UnloadManifest.class);

            List<S3PolledFile> s3PolledFiles = polledFilesManifest.getEntries().stream()
                    .filter(manifestEntry -> manifestEntry.getMeta().getContentLength() > 0)
                    .map(RedshiftDataPoller::s3PolledFile).collect(Collectors.toList());

            RecordInputStream recordInputStream = new S3FilesRecordInputStream(querySchema, new RedshiftCsvSchemaMapper(), s3PolledFiles, s3Client, FileFormat.CSV,
                    getPipelineRunUnloadDirectory(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId()),
                    20, true);
            return WarehousePollResult.builder()
                    .recordInputStream(recordInputStream).warehouseSchema(querySchema).resumed(true).build();

        } catch (Exception e) {
            log.error("Resume Poll records from warehouse {} failed", warehousePollContext.getWarehouseConfig().getType(), e);
            return pollRecords(warehousePollContext);
        }
    }

    private RecordSchema getQuerySchema(RedshiftWarehouseConfig redshiftWarehouseConfig,
                                        String query) throws SQLException {

        try (Connection connection = ObjectRegistry.getInstance(RedshiftConnector.class).getConnection(redshiftWarehouseConfig)) {
            return getSchemaFromQuery(connection, query);
        }

    }

    @Override
    public void cleanupPipelineResources(String pipelineUUID, WarehouseConfig warehouseConfig) {
        try {
            RedshiftConnector redshiftConnector = ObjectRegistry.getInstance(RedshiftConnector.class);
            try (Connection connection = redshiftConnector.getConnection((RedshiftWarehouseConfig) warehouseConfig)) {
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

    private void createUncommittedSnapshot(Connection connection, WarehousePollContext warehousePollContext, List<String> internalTables) throws SQLException {

        if (internalTables.contains(ConnectorExecutionConstants.getUncommittedSnapshot(warehousePollContext.getPipelineUUID()))) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("drop table if exists %s",
                        ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID())));
            }
        }
        RedshiftConnector redshiftConnector = ObjectRegistry.getInstance(RedshiftConnector.class);
        RedshiftTableProperties redshiftTableProperties = (RedshiftTableProperties) redshiftConnector.getSnapshotTableProperties(warehousePollContext.getPrimaryKeys());
        this.redshiftClient.createTableFromQuery(connection, ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID()),
                warehousePollContext.getQuery(), redshiftTableProperties, false);
    }

    private RecordInputStream createRecordStream(Connection connection, WarehousePollContext warehousePollContext,
                                                 List<String> bookKeepingTables, RecordSchema querySchema) throws SQLException, IOException {
        RedshiftWarehouseConfig warehouseConfig = (RedshiftWarehouseConfig) warehousePollContext.getWarehouseConfig();
        CastledS3Client s3Client = new CastledS3Client(warehouseConfig.getAccessKeyId(),
                warehouseConfig.getAccessKeySecret(), warehousePollContext.getDataEncryptionKey(), warehouseConfig.getRegion(), warehouseConfig.getS3Bucket());
        String bucket = s3Client.getBucket();
        String unloadDirectory = CastledS3Client.constructS3Path(bucket,
                Lists.newArrayList(FileStorageNamespace.PIPELINE_UNLOADS.getNamespace(), warehousePollContext.getPipelineUUID(),
                        String.valueOf(warehousePollContext.getPipelineRunId())));

        String unloadDirectoryKey = getS3UnloadDirectory(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId());


        String unloadQuery = String.format("unload ('%s') to '%s/' CREDENTIALS " +
                        "'aws_access_key_id=%s;aws_secret_access_key=%s;master_symmetric_key=%s' ENCRYPTED maxfilesize %d MB " +
                        "region '%s' format CSV  allowoverwrite manifest header gzip",
                getDataFetchQuery(warehousePollContext, bookKeepingTables), unloadDirectory, warehouseConfig.getAccessKeyId(),
                warehouseConfig.getAccessKeySecret(), s3Client.getEncryptionKey(),
                warehouseConnectorConfig.getUnloadFileSize(), s3Client.getRegion().getName());

        try (Statement statement = connection.createStatement()) {
            statement.execute(unloadQuery);
        }

        String manifestFilePath = unloadDirectoryKey + CastledS3Client.PATH_SEPARATOR + "manifest";
        String manifest = s3Client.getObjectAsString(manifestFilePath);
        RedshiftS3UnloadManifest polledFilesManifest = JsonUtils.jsonStringToObject(manifest, RedshiftS3UnloadManifest.class);

        List<S3PolledFile> s3PolledFiles = polledFilesManifest.getEntries().stream()
                .filter(manifestEntry -> manifestEntry.getMeta().getContentLength() > 0)
                .map(RedshiftDataPoller::s3PolledFile).collect(Collectors.toList());

        return new S3FilesRecordInputStream(querySchema, new RedshiftCsvSchemaMapper(), s3PolledFiles, s3Client, FileFormat.CSV,
                getPipelineRunUnloadDirectory(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId()),
                20, true);
    }

    private String getDataFetchQuery(WarehousePollContext warehousePollRequest, List<String> bookKeepingTables) {
        String committedSnapshot = ConnectorExecutionConstants.getQualifiedCommittedSnapshot(warehousePollRequest.getPipelineUUID());
        String uncommittedSnapshot = ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollRequest.getPipelineUUID());
        if (bookKeepingTables.contains(ConnectorExecutionConstants.getCommittedSnapshot(warehousePollRequest.getPipelineUUID()))) {
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
        return RedshiftUtils.getS3Client(warehouseConfig, encryptionKey);
    }
}
