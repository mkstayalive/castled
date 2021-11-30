package io.castled.pipelines;

import com.google.inject.Inject;
import io.castled.AppShutdownHandler;
import io.castled.ObjectRegistry;
import io.castled.apps.ExternalApp;
import io.castled.apps.ExternalAppConnector;
import io.castled.apps.ExternalAppService;
import io.castled.apps.ExternalAppType;
import io.castled.apps.models.DataSinkRequest;
import io.castled.commons.models.PipelineSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.commons.streams.MessageInputStreamImpl;
import io.castled.constants.CommonConstants;
import io.castled.encryption.EncryptionManager;
import io.castled.errors.MysqlErrorTracker;
import io.castled.errors.SchemaMappedErrorTracker;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.exceptions.pipeline.PipelineExecutionException;
import io.castled.jarvis.taskmanager.TaskExecutor;
import io.castled.jarvis.taskmanager.exceptions.JarvisRetriableException;
import io.castled.jarvis.taskmanager.models.Task;
import io.castled.models.*;
import io.castled.pipelines.exceptions.PipelineInterruptedException;
import io.castled.schema.SchemaUtils;
import io.castled.schema.models.RecordSchema;
import io.castled.services.PipelineService;
import io.castled.utils.PipelineUtils;
import io.castled.warehouses.WarehouseConnector;
import io.castled.warehouses.WarehouseService;
import io.castled.warehouses.WarehouseSyncFailureListener;
import io.castled.warehouses.WarehouseType;
import io.castled.warehouses.models.WarehousePollContext;
import io.castled.warehouses.models.WarehousePollResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked"})
@Slf4j
public class PipelineExecutor implements TaskExecutor {

    private final PipelineService pipelineService;
    private final Map<WarehouseType, WarehouseConnector> warehouseConnectors;
    private final Map<ExternalAppType, ExternalAppConnector> externalAppConnectors;
    private final WarehouseService warehouseService;
    private final ExternalAppService externalAppService;
    private final EncryptionManager encryptionManager;
    private final MonitoredDataSink monitoredDataSink;

    @Inject
    public PipelineExecutor(PipelineService pipelineService, Map<WarehouseType, WarehouseConnector> warehouseConnectors,
                            WarehouseService warehouseService, Map<ExternalAppType, ExternalAppConnector> externalAppConnectors,
                            ExternalAppService externalAppService, EncryptionManager encryptionManager,
                            MonitoredDataSink monitoredDataSink) {
        this.pipelineService = pipelineService;
        this.warehouseConnectors = warehouseConnectors;
        this.warehouseService = warehouseService;
        this.externalAppConnectors = externalAppConnectors;
        this.externalAppService = externalAppService;
        this.encryptionManager = encryptionManager;
        this.monitoredDataSink = monitoredDataSink;
    }

    @Override
    public String executeTask(Task task) {
        Long pipelineId = ((Number) task.getParams().get(CommonConstants.PIPELINE_ID)).longValue();
        Pipeline pipeline = this.pipelineService.getActivePipeline(pipelineId);
        if (pipeline == null) {
            return null;
        }
        WarehouseSyncFailureListener warehouseSyncFailureListener = null;
        Warehouse warehouse = this.warehouseService.getWarehouse(pipeline.getWarehouseId());
        PipelineRun pipelineRun = getOrCreatePipelineRun(pipelineId);
        WarehousePollContext warehousePollContext = WarehousePollContext.builder()
                .primaryKeys(PipelineUtils.getWarehousePrimaryKeys(pipeline)).pipelineUUID(pipeline.getUuid())
                .pipelineRunId(pipelineRun.getId()).warehouseConfig(warehouse.getConfig())
                .dataEncryptionKey(encryptionManager.getEncryptionKey(warehouse.getTeamId()))
                .queryMode(pipeline.getQueryMode())
                .query(pipeline.getSourceQuery()).pipelineId(pipeline.getId()).build();
        try {

            WarehouseExecutionContext warehouseExecutionContext = pollRecords(warehouse, pipelineRun, warehousePollContext);

            log.info("Poll records completed for pipeline {}", pipeline.getName());
            this.pipelineService.updatePipelineRunstage(pipelineRun.getId(), PipelineRunStage.RECORDS_POLLED);

            ExternalApp externalApp = externalAppService.getExternalApp(pipeline.getAppId());
            ExternalAppConnector externalAppConnector = this.externalAppConnectors.get(externalApp.getType());
            RecordSchema appSchema = externalAppConnector.getSchema(externalApp.getConfig(), pipeline.getAppSyncConfig())
                    .getAppSchema();

            log.info("App schema fetch completed for pipeline {}", pipeline.getName());

            warehousePollContext.setWarehouseSchema(warehouseExecutionContext.getWarehouseSchema());
            warehouseSyncFailureListener = warehouseConnectors.get(warehouse.getType())
                    .syncFailureListener(warehousePollContext);

            MysqlErrorTracker mysqlErrorTracker = new MysqlErrorTracker(warehousePollContext);

            ErrorOutputStream schemaMappingErrorOutputStream = new ErrorOutputStream(warehouseSyncFailureListener, mysqlErrorTracker);

            SchemaMappedMessageInputStream schemaMappedMessageInputStream = new SchemaMappedMessageInputStream(
                    appSchema, warehouseExecutionContext.getMessageInputStreamImpl(), pipeline.getDataMapping().appWarehouseMapping(),
                    pipeline.getDataMapping().warehouseAppMapping(), schemaMappingErrorOutputStream);

            SchemaMappedRecordOutputStream schemaMappedRecordOutputStream =
                    new SchemaMappedRecordOutputStream(SchemaUtils.filterSchema(warehousePollContext.getWarehouseSchema(),
                            PipelineUtils.getWarehousePrimaryKeys(pipeline)), warehouseSyncFailureListener,
                            pipeline.getDataMapping().warehouseAppMapping());

            ErrorOutputStream sinkErrorOutputStream = new ErrorOutputStream(schemaMappedRecordOutputStream,
                    new SchemaMappedErrorTracker(mysqlErrorTracker, warehouseExecutionContext.getWarehouseSchema(), pipeline.getDataMapping().warehouseAppMapping()));

            log.info("App Sync started for pipeline {}", pipeline.getName());

            List<String> mappedAppFields = pipeline.getDataMapping().getFieldMappings().stream().filter(mapping -> !mapping.isSkipped())
                    .map(FieldMapping::getAppField).collect(Collectors.toList());

            DataSinkRequest dataSinkRequest = DataSinkRequest.builder().externalApp(externalApp).errorOutputStream(sinkErrorOutputStream)
                    .appSyncConfig(pipeline.getAppSyncConfig()).mappedFields(mappedAppFields)
                    .objectSchema(appSchema).primaryKeys(pipeline.getDataMapping().getPrimaryKeys())
                    .messageInputStream(schemaMappedMessageInputStream)
                    .build();

            PipelineSyncStats pipelineSyncStats = monitoredDataSink.syncRecords(externalAppConnector.getDataSink(),
                    pipelineRun.getPipelineSyncStats(), pipelineRun.getId(), dataSinkRequest);

            schemaMappedMessageInputStream.close();

            log.info("App Sync completed for pipeline {}", pipeline.getName());
            //flush output streams
            schemaMappingErrorOutputStream.flushFailedRecords();
            sinkErrorOutputStream.flushFailedRecords();

            warehouseConnectors.get(warehouse.getType()).getDataPoller().cleanupPipelineRunResources(warehousePollContext);
            // Also add the records that failed schema mapping phase to the final stats
            pipelineSyncStats.setRecordsFailed(schemaMappedMessageInputStream.getFailedRecords() + pipelineSyncStats.getRecordsFailed());
            this.pipelineService.markPipelineRunProcessed(pipelineRun.getId(), pipelineSyncStats);

        } catch (Exception e) {
            if (ObjectRegistry.getInstance(AppShutdownHandler.class).isShutdownTriggered()) {
                throw new PipelineInterruptedException();
            }
            this.pipelineService.markPipelineRunFailed(pipelineRun.getId(), Optional.ofNullable(e.getMessage()).orElse("Unknown Error"));
            log.error("Pipeline run failed for pipeline {} ", pipeline.getId(), e);
            this.warehouseConnectors.get(warehouse.getType()).getDataPoller().cleanupPipelineRunResources(warehousePollContext);
            Optional.ofNullable(warehouseSyncFailureListener).ifPresent(syncFailureListener ->
                    syncFailureListener.cleanupResources(pipeline.getUuid(), pipelineRun.getId(), warehouse.getConfig()));

            if (e instanceof PipelineExecutionException) {
                handlePipelineExecutionException(pipeline, (PipelineExecutionException) e);
            } else {
                log.error("Pipeline run failed for pipeline {} ", pipeline.getId(), e);
            }
        }
        return null;
    }

    private PipelineRun getOrCreatePipelineRun(Long pipelineId) {
        PipelineRun lastRun = this.pipelineService.getLastRun(pipelineId);
        if (lastRun != null && lastRun.getStatus().equals(PipelineRunStatus.PROCESSING)) {
            return lastRun;
        }
        long pipelineRunId = this.pipelineService.createPipelineRun(pipelineId);
        return this.pipelineService.getPipelineRun(pipelineRunId);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class WarehouseExecutionContext {
        private MessageInputStreamImpl messageInputStreamImpl;
        private RecordSchema warehouseSchema;
    }


    private WarehouseExecutionContext pollRecords(
            Warehouse warehouse, PipelineRun pipelineRun,
            WarehousePollContext warehousePollContext) throws Exception {

        WarehouseConnector warehouseConnector = this.warehouseConnectors.get(warehouse.getType());
        WarehousePollResult warehousePollResult;
        if (pipelineRun.getStage().recordsPolled()) {
            warehousePollResult = warehouseConnector.getDataPoller().resumePoll(warehousePollContext);
        } else {
            warehousePollResult = warehouseConnector.getDataPoller().pollRecords(warehousePollContext);
        }
        MessageInputStreamImpl messageInputStream = new MessageInputStreamImpl(warehousePollResult.getRecordInputStream(),
                warehousePollResult.isResumed() ? pipelineRun.getPipelineSyncStats().getOffset() : 0);
        return new WarehouseExecutionContext(messageInputStream, warehousePollResult.getWarehouseSchema());
    }

    private void handlePipelineExecutionException(Pipeline pipeline, PipelineExecutionException
            pipelineExecutionException) {

        switch (pipelineExecutionException.getPipelineError().getPipelineErrorType()) {
            case INTERMITTENT:
                log.error("Pipeline run failed for pipeline {} ", pipeline.getId(), pipelineExecutionException);
                throw new JarvisRetriableException(pipelineExecutionException.getErrorMessage());
            case INTERNAL:
            case USER_ACTIION_REQUIRED:
                log.error("Pipeline run failed for pipeline {} ", pipeline.getId(), pipelineExecutionException);
                break;
            default:
                throw new CastledRuntimeException(String.format("Invalid error type: %s", pipelineExecutionException.getPipelineError().getPipelineErrorType()));
        }
    }
}
