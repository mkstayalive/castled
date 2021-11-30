package io.castled.warehouses.connectors.bigquery;

import com.google.cloud.bigquery.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.castled.ObjectRegistry;
import io.castled.commons.models.FileStorageNamespace;
import io.castled.constants.ConnectorExecutionConstants;
import io.castled.filemanager.RawFileWriter;
import io.castled.filestorage.GcsClient;
import io.castled.models.QueryMode;
import io.castled.schema.models.FieldSchema;
import io.castled.schema.models.Tuple;
import io.castled.utils.FileUtils;
import io.castled.utils.JsonUtils;
import io.castled.utils.SizeUtils;
import io.castled.warehouses.WarehouseConfig;
import io.castled.warehouses.WarehouseConnectorConfig;
import io.castled.warehouses.WarehouseSyncFailureListener;
import io.castled.warehouses.connectors.bigquery.daos.BQSnapshotTrackerDAO;
import io.castled.warehouses.connectors.bigquery.gcp.GcpClientFactory;
import io.castled.warehouses.models.WarehousePollContext;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class BQSyncFailureListener extends WarehouseSyncFailureListener {
    private final WarehousePollContext warehousePollContext;
    private final RawFileWriter rawFileWriter;
    private final BigQueryWarehouseConfig warehouseConfig;
    private final String gcsUploadDirectory;
    private final GcsClient gcsClient;
    private final String bucket;
    private final List<String> gcsFileUrls = Lists.newArrayList();
    private final BQSnapshotTrackerDAO bqSnapshotTrackerDAO;
    private int totalBytes = 0;
    private long failedRecords = 0;

    public BQSyncFailureListener(WarehousePollContext warehousePollContext) {
        super(warehousePollContext);
        this.warehousePollContext = warehousePollContext;
        this.rawFileWriter = new RawFileWriter(SizeUtils.convertMBToBytes(50), failureRecordsDirectory,
                () -> UUID.randomUUID().toString());
        this.warehouseConfig = (BigQueryWarehouseConfig) warehousePollContext.getWarehouseConfig();

        this.gcsClient = ObjectRegistry.getInstance(GcpClientFactory.class).getGcsClient(warehouseConfig.getServiceAccount(),
                warehouseConfig.getProjectId());
        this.gcsUploadDirectory = GcsClient.constructObjectKey(Lists.newArrayList(FileStorageNamespace.PIPELINE_FAILED_RECORDS.getNamespace(),
                warehousePollContext.getPipelineUUID(), String.valueOf(warehousePollContext.getPipelineRunId())));
        this.bucket = warehouseConfig.getBucketName();
        this.bqSnapshotTrackerDAO = ObjectRegistry.getInstance(Jdbi.class).onDemand(BQSnapshotTrackerDAO.class);
    }

    @Override
    public void cleanupResources(String pipelineUUID, Long pipelineRunId, WarehouseConfig warehouseConfig) {
        BigQueryWarehouseConfig bigQueryWarehouseConfig = (BigQueryWarehouseConfig) warehouseConfig;
        GcsClient gcsClient = ObjectRegistry.getInstance(GcpClientFactory.class).getGcsClient(bigQueryWarehouseConfig.getServiceAccount(),
                bigQueryWarehouseConfig.getProjectId());
        gcsClient.deleteDirectory(bigQueryWarehouseConfig.getBucketName(), gcsUploadDirectory);
        FileUtils.deleteDirectory(failureRecordsDirectory);
    }

    @Override
    public void doFlush() throws Exception {
        if (warehousePollContext.getQueryMode() == QueryMode.FULL_LOAD) {
            return;
        }
        if (totalBytes > 0) {
            this.rawFileWriter.close();
            uploadFilesToGCS();
        }

        BigQuery bigQuery = ObjectRegistry.getInstance(GcpClientFactory.class)
                .getBigQuery(warehouseConfig.getServiceAccount(), warehouseConfig.getProjectId());
        BQSnapshotTracker bqSnapshotTracker = bqSnapshotTrackerDAO.getSnapshotTracker(warehousePollContext.getPipelineUUID());
        if (failedRecords > 0) {
            removeFailedRecordsFromSnapshot(bigQuery, bqSnapshotTracker);
        }
        commitSnapshot(bigQuery, bqSnapshotTracker);
    }

    private void commitSnapshot(BigQuery bigQuery, BQSnapshotTracker bqSnapshotTracker) {
        String prevCommittedSnapshot = bqSnapshotTracker.getCommittedSnapshot();
        bqSnapshotTrackerDAO.commitSnapshot(warehousePollContext.getPipelineUUID());
        if (prevCommittedSnapshot != null) {
            bigQuery.delete(TableId.of(ConnectorExecutionConstants.CASTLED_CONTAINER, prevCommittedSnapshot));
        }
    }


    private void removeFailedRecordsFromSnapshot(BigQuery bigQuery, BQSnapshotTracker bqSnapshotTracker) throws Exception {

        String failedRecordsTable = ConnectorExecutionConstants.getFailedRecordsTable(warehousePollContext.getPipelineUUID());

        //create failed records table
        String failedRecordsCreateQuery = String.format("select %s from %s.%s limit 0",
                String.join(",", trackableFields), ConnectorExecutionConstants.CASTLED_CONTAINER, bqSnapshotTracker.getUncommittedSnapshot());
        bigQuery.query(QueryJobConfiguration.newBuilder(failedRecordsCreateQuery).setDestinationTable(TableId.of(ConnectorExecutionConstants.CASTLED_CONTAINER, failedRecordsTable))
                .setWriteDisposition(JobInfo.WriteDisposition.WRITE_TRUNCATE).build());

        //copy failed records to failed records table
        LoadJobConfiguration loadJobConfiguration = LoadJobConfiguration
                .newBuilder(TableId.of(ConnectorExecutionConstants.CASTLED_CONTAINER,
                        ConnectorExecutionConstants.getFailedRecordsTable(warehousePollContext.getPipelineUUID())), gcsFileUrls, FormatOptions.json())
                .setIgnoreUnknownValues(true).build();
        BigQueryUtils.runJob(bigQuery.create(JobInfo.newBuilder(loadJobConfiguration).build()));

        gcsClient.deleteDirectory(bucket, gcsUploadDirectory);

        //delete failed results from uncommitted snapshot table
        StringBuilder mergeQueryBuilder = new StringBuilder(String.format("MERGE %s.%s T using %s.%s S on 1 = 1",
                ConnectorExecutionConstants.CASTLED_CONTAINER, bqSnapshotTracker.getUncommittedSnapshot(),
                ConnectorExecutionConstants.CASTLED_CONTAINER, failedRecordsTable));
        for (String trackableField : trackableFields) {
            mergeQueryBuilder.append(String.format(" AND (T.%s = S.%s OR (T.%s IS NULL and S.%s IS NULL))",
                    trackableField, trackableField, trackableField, trackableField));
        }
        mergeQueryBuilder.append(" WHEN MATCHED THEN DELETE");
        BigQueryUtils.runJob(bigQuery.create(JobInfo.newBuilder(QueryJobConfiguration.newBuilder(mergeQueryBuilder.toString()).build()).build()));

        bigQuery.delete(TableId.of(ConnectorExecutionConstants.CASTLED_CONTAINER, ConnectorExecutionConstants.getFailedRecordsTable(warehousePollContext.getPipelineUUID())));
    }

    @Override
    public synchronized void doWriteRecord(Tuple tuple) throws Exception {
        if (warehousePollContext.getQueryMode() == QueryMode.FULL_LOAD) {
            return;
        }
        String record = getCopyableRecord(tuple);
        byte[] recordBytes = record.getBytes();
        this.rawFileWriter.writeRecord(recordBytes);
        totalBytes += recordBytes.length;
        failedRecords++;
        if (totalBytes > SizeUtils.convertGBToBytes(ObjectRegistry.getInstance(WarehouseConnectorConfig.class).getFailedRecordBufferSize())) {
            this.rawFileWriter.close();
            uploadFilesToGCS();
            if (!Files.exists(failureRecordsDirectory)) {
                Files.createDirectory(failureRecordsDirectory);
            }
        }
    }

    private void uploadFilesToGCS() throws Exception {
        this.gcsFileUrls.addAll(this.gcsClient.uploadDirectory(bucket, failureRecordsDirectory, gcsUploadDirectory));
        FileUtils.deleteDirectory(failureRecordsDirectory);
    }

    private String getCopyableRecord(Tuple record) {
        BQWarehouseCopyAdaptor bqWarehouseCopyAdaptor = ObjectRegistry.getInstance(BQWarehouseCopyAdaptor.class);
        Map<String, Object> copyableValues = Maps.newHashMap();
        for (FieldSchema fieldSchema : warehousePollContext.getWarehouseSchema().getFieldSchemas()) {
            if (trackableFields.contains(fieldSchema.getName())) {
                copyableValues.put(fieldSchema.getName(),
                        bqWarehouseCopyAdaptor.constructSyncableRecord(record.getValue(fieldSchema.getName()), fieldSchema.getSchema()));
            }
        }
        return JsonUtils.objectToString(copyableValues);
    }
}
