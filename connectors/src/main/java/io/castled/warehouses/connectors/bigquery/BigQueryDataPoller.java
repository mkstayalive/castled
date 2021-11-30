package io.castled.warehouses.connectors.bigquery;

import com.google.cloud.bigquery.*;
import com.google.cloud.storage.Blob;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.commons.models.FileFormat;
import io.castled.commons.models.FileStorageNamespace;
import io.castled.commons.streams.GcsFilesRecordInputStream;
import io.castled.commons.streams.RecordInputStream;
import io.castled.constants.ConnectorExecutionConstants;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.filestorage.GcsClient;
import io.castled.models.QueryMode;
import io.castled.schema.models.RecordSchema;
import io.castled.utils.FileUtils;
import io.castled.warehouses.WarehouseConfig;
import io.castled.warehouses.WarehouseDataPoller;
import io.castled.warehouses.connectors.bigquery.daos.BQSnapshotTrackerDAO;
import io.castled.warehouses.connectors.bigquery.gcp.GcpClientFactory;
import io.castled.warehouses.models.WarehousePollContext;
import io.castled.warehouses.models.WarehousePollResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
public class BigQueryDataPoller implements WarehouseDataPoller {

    private final GcpClientFactory gcpClientFactory;
    private final BigQueryConnector bigQueryConnector;
    private final BigQueryExportJsonSchemaMapper bigQueryExportJsonSchemaMapper;
    private final BQSnapshotTrackerDAO bqSnapshotTrackerDAO;

    @Inject
    public BigQueryDataPoller(GcpClientFactory gcpClientFactory, BigQueryConnector bigQueryConnector,
                              BigQueryExportJsonSchemaMapper bigQueryExportJsonSchemaMapper, Jdbi jdbi) {
        this.gcpClientFactory = gcpClientFactory;
        this.bigQueryConnector = bigQueryConnector;
        this.bigQueryExportJsonSchemaMapper = bigQueryExportJsonSchemaMapper;
        this.bqSnapshotTrackerDAO = jdbi.onDemand(BQSnapshotTrackerDAO.class);
    }

    @Override
    public WarehousePollResult pollRecords(WarehousePollContext warehousePollContext) {
        try {

            BigQueryWarehouseConfig bigQueryWarehouseConfig = (BigQueryWarehouseConfig) warehousePollContext.getWarehouseConfig();
            BigQuery bigQuery = gcpClientFactory.getBigQuery(bigQueryWarehouseConfig.getServiceAccount(),
                    bigQueryWarehouseConfig.getProjectId());
            if (warehousePollContext.getQueryMode() == QueryMode.FULL_LOAD) {
                return doFullLoad(bigQuery, warehousePollContext);
            }

            List<String> bookKeepingTables = BigQueryUtils.listTables(ConnectorExecutionConstants.CASTLED_CONTAINER, bigQuery);
            if (CollectionUtils.isEmpty(bookKeepingTables)) {
                BigQueryUtils.getOrCreateDataset(ConnectorExecutionConstants.CASTLED_CONTAINER, bigQuery, bigQueryWarehouseConfig.getLocation());
            }
            BQSnapshotTracker bqSnapshotTracker = getOrCreateSnapshotTracker(warehousePollContext.getPipelineUUID());
            dropOrphanedTables(bookKeepingTables, bqSnapshotTracker, warehousePollContext.getPipelineUUID(), bigQuery);

            String uncommittedSnapshot = createUncommittedSnapshot(bigQuery, warehousePollContext);
            bqSnapshotTracker.setUncommittedSnapshot(uncommittedSnapshot);
            RecordSchema querySchema = bigQueryConnector.getQuerySchema
                    (bigQueryWarehouseConfig, String.format("select * from %s.%s", ConnectorExecutionConstants.CASTLED_CONTAINER, uncommittedSnapshot));

            return WarehousePollResult.builder()
                    .recordInputStream(createRecordStream(bigQuery, bigQueryWarehouseConfig,
                            warehousePollContext, getDataFetchQuery(bookKeepingTables, bqSnapshotTracker), querySchema)).warehouseSchema(querySchema).build();

        } catch (Exception e) {
            log.error("Data poll failed for pipeline {} and pipeline run {}", warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId());
            throw new CastledRuntimeException(e);
        }
    }

    public WarehousePollResult doFullLoad(BigQuery bigQuery, WarehousePollContext warehousePollContext) throws Exception {
        BigQueryWarehouseConfig bigQueryWarehouseConfig = (BigQueryWarehouseConfig) warehousePollContext.getWarehouseConfig();
        RecordSchema querySchema = bigQueryConnector.getQuerySchema(bigQueryWarehouseConfig, warehousePollContext.getQuery());
        return WarehousePollResult.builder()
                .recordInputStream(createRecordStream(bigQuery, bigQueryWarehouseConfig,
                        warehousePollContext, warehousePollContext.getQuery(), querySchema)).warehouseSchema(querySchema).build();

    }

    @Override
    public WarehousePollResult resumePoll(WarehousePollContext warehousePollContext) {
        try {
            BigQueryWarehouseConfig bigQueryWarehouseConfig = (BigQueryWarehouseConfig) warehousePollContext.getWarehouseConfig();
            GcsClient gcsClient = gcpClientFactory.getGcsClient(bigQueryWarehouseConfig.getServiceAccount(),
                    bigQueryWarehouseConfig.getProjectId());

            List<Blob> blobs = gcsClient.listObjects(bigQueryWarehouseConfig.getBucketName(),
                    getPipelineRunGcsUnloadDir(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId()));
            if (CollectionUtils.isEmpty(blobs)) {
                return pollRecords(warehousePollContext);
            }

            String dataFetchQuery = warehousePollContext.getQueryMode() == QueryMode.FULL_LOAD ? warehousePollContext.getQuery() :
                    String.format("select * from %s.%s", ConnectorExecutionConstants.CASTLED_CONTAINER,
                            bqSnapshotTrackerDAO.getSnapshotTracker(warehousePollContext.getPipelineUUID()).getUncommittedSnapshot());
            RecordSchema querySchema = bigQueryConnector.getQuerySchema(bigQueryWarehouseConfig, dataFetchQuery);

            GcsFilesRecordInputStream gcsFilesRecordInputStream = new GcsFilesRecordInputStream(querySchema, bigQueryExportJsonSchemaMapper, blobs,
                    FileFormat.JSON, getPipelineRunUnloadDirectory(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId()),
                    gcsClient, 20, true);

            return WarehousePollResult.builder().recordInputStream(gcsFilesRecordInputStream)
                    .warehouseSchema(querySchema).resumed(true).build();
        } catch (Exception e) {
            log.error("Data poll resume failed for pipeline {} and pipeline run {}", warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId());
            return pollRecords(warehousePollContext);
        }

    }

    private void dropOrphanedTables(List<String> bookKeepingTables, BQSnapshotTracker bqSnapshotTracker,
                                    String pipelineUUID, BigQuery bigQuery) {
        for (String bookKeepingTable : bookKeepingTables) {
            if (bookKeepingTable.startsWith(pipelineUUID) && !bookKeepingTable.equals(bqSnapshotTracker.getCommittedSnapshot())) {
                bigQuery.delete(TableId.of(ConnectorExecutionConstants.CASTLED_CONTAINER, bookKeepingTable));
            }
        }
    }

    private BQSnapshotTracker getOrCreateSnapshotTracker(String pipelineUUID) {
        BQSnapshotTracker bqSnapshotTracker = bqSnapshotTrackerDAO.getSnapshotTracker(pipelineUUID);
        if (bqSnapshotTracker != null) {
            return bqSnapshotTracker;
        }
        Long snapshotTrackerId = bqSnapshotTrackerDAO.createPipelineSnapshot(pipelineUUID, null, null);
        return BQSnapshotTracker.builder().id(snapshotTrackerId).pipelineUUID(pipelineUUID).build();
    }


    private String createUncommittedSnapshot(BigQuery bigQuery, WarehousePollContext warehousePollContext) throws Exception {

        TableId uncommittedSnapshotTable = TableId.of(ConnectorExecutionConstants.CASTLED_CONTAINER,
                String.format("%s_snapshot_%d", warehousePollContext.getPipelineUUID(), System.currentTimeMillis()));
        bigQuery.query(QueryJobConfiguration.newBuilder(warehousePollContext.getQuery())
                .setDestinationTable(uncommittedSnapshotTable)
                .setWriteDisposition(JobInfo.WriteDisposition.WRITE_TRUNCATE).build());
        this.bqSnapshotTrackerDAO.updateUncommittedSnapshot(warehousePollContext.getPipelineUUID(), uncommittedSnapshotTable.getTable());
        return uncommittedSnapshotTable.getTable();
    }


    private RecordInputStream createRecordStream(BigQuery bigQuery, BigQueryWarehouseConfig bigQueryWarehouseConfig,
                                                 WarehousePollContext warehousePollContext, String dataFetchQuery, RecordSchema querySchema) throws Exception {

        GcsClient gcsClient = gcpClientFactory.getGcsClient(bigQueryWarehouseConfig.getServiceAccount(),
                bigQueryWarehouseConfig.getProjectId());
        String unloadDirectoryPath = GcsClient.constructGcsPath(bigQueryWarehouseConfig.getBucketName(),
                Lists.newArrayList(FileStorageNamespace.PIPELINE_UNLOADS.getNamespace(), warehousePollContext.getPipelineUUID(),
                        String.valueOf(warehousePollContext.getPipelineRunId()), "*.json.gz"));

        String unloadQuery = String.format("EXPORT DATA OPTIONS (uri = '%s',compression = 'GZIP', format='JSON', overwrite=true)" +
                " AS %s", unloadDirectoryPath, dataFetchQuery);

        JobConfiguration jobConfiguration = QueryJobConfiguration.newBuilder(unloadQuery).build();
        BigQueryUtils.runJob(bigQuery.create(JobInfo.newBuilder(jobConfiguration).build()));
        List<Blob> blobs = gcsClient.listObjects(bigQueryWarehouseConfig.getBucketName(),
                getPipelineRunGcsUnloadDir(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId()));

        return new GcsFilesRecordInputStream(querySchema, bigQueryExportJsonSchemaMapper, blobs,
                FileFormat.JSON, getPipelineRunUnloadDirectory(warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId()),
                gcsClient, 20, true);

    }

    private String getDataFetchQuery(List<String> bookKeepingTables, BQSnapshotTracker bqSnapshotTracker) {
        String committedSnapshot = bqSnapshotTracker.getCommittedSnapshot();
        String uncommittedSnapshot = bqSnapshotTracker.getUncommittedSnapshot();
        if (Optional.ofNullable(committedSnapshot).filter(bookKeepingTables::contains).isPresent()) {
            return String.format("select * from %s.%s except distinct select * from %s.%s", ConnectorExecutionConstants.CASTLED_CONTAINER, uncommittedSnapshot,
                    ConnectorExecutionConstants.CASTLED_CONTAINER, committedSnapshot);
        }
        return String.format("select * from %s.%s", ConnectorExecutionConstants.CASTLED_CONTAINER, uncommittedSnapshot);
    }

    @Override
    public void cleanupPipelineRunResources(WarehousePollContext warehousePollContext) {

        BigQueryWarehouseConfig bigQueryWarehouseConfig = (BigQueryWarehouseConfig) warehousePollContext.getWarehouseConfig();
        GcsClient gcsClient = ObjectRegistry.getInstance(GcpClientFactory.class).getGcsClient(bigQueryWarehouseConfig.getServiceAccount(),
                bigQueryWarehouseConfig.getProjectId());
        gcsClient.deleteDirectory(bigQueryWarehouseConfig.getBucketName(), getPipelineRunGcsUnloadDir
                (warehousePollContext.getPipelineUUID(), warehousePollContext.getPipelineRunId()));
        FileUtils.deleteDirectory(getPipelineRunUnloadDirectory(warehousePollContext.getPipelineUUID(),
                warehousePollContext.getPipelineRunId()));
    }

    public String getPipelineGcsUnloadDir(String pipelineUUID) {
        return GcsClient.constructObjectKey(Lists.newArrayList(FileStorageNamespace.PIPELINE_UNLOADS.getNamespace(), pipelineUUID));
    }

    public String getPipelineRunGcsUnloadDir(String pipelineUUID, Long pipelineRunId) {
        return GcsClient.constructObjectKey(Lists.newArrayList(FileStorageNamespace.PIPELINE_UNLOADS.getNamespace(),
                pipelineUUID, String.valueOf(pipelineRunId)));
    }

    @Override
    public void cleanupPipelineResources(String pipelineUUID, WarehouseConfig warehouseConfig) {
        BigQueryWarehouseConfig bigQueryWarehouseConfig = (BigQueryWarehouseConfig) warehouseConfig;
        GcsClient gcsClient = ObjectRegistry.getInstance(GcpClientFactory.class).getGcsClient(bigQueryWarehouseConfig.getServiceAccount(),
                bigQueryWarehouseConfig.getProjectId());
        gcsClient.deleteDirectory(bigQueryWarehouseConfig.getBucketName(), getPipelineGcsUnloadDir(pipelineUUID));
        BigQuery bigQuery = ObjectRegistry.getInstance(GcpClientFactory.class).getBigQuery(bigQueryWarehouseConfig.getServiceAccount(),
                bigQueryWarehouseConfig.getProjectId());
        for (String internalTable : BigQueryUtils.listTables(ConnectorExecutionConstants.CASTLED_CONTAINER, bigQuery)) {
            bigQuery.delete(TableId.of(ConnectorExecutionConstants.CASTLED_CONTAINER, internalTable));
        }
    }
}
