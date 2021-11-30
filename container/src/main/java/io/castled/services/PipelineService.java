package io.castled.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.apps.ExternalApp;
import io.castled.apps.ExternalAppConnector;
import io.castled.apps.ExternalAppService;
import io.castled.apps.ExternalAppType;
import io.castled.apps.dtos.AppSyncConfigDTO;
import io.castled.apps.models.ExternalAppSchema;
import io.castled.caches.PipelineCache;
import io.castled.commons.models.PipelineSyncStats;
import io.castled.constants.CommonConstants;
import io.castled.daos.ErrorReportsDAO;
import io.castled.daos.PipelineDAO;
import io.castled.daos.PipelineRunDAO;
import io.castled.dtos.PipelineConfigDTO;
import io.castled.dtos.PipelineSchema;
import io.castled.dtos.PipelineUpdateRequest;
import io.castled.errors.PipelineErrorAndSample;
import io.castled.errors.PipelineRunErrors;
import io.castled.events.CastledEventsClient;
import io.castled.events.pipelineevents.PipelineEvent;
import io.castled.events.pipelineevents.PipelineEventType;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.jarvis.JarvisTaskGroup;
import io.castled.jarvis.JarvisTaskType;
import io.castled.jarvis.taskmanager.JarvisTasksClient;
import io.castled.jarvis.taskmanager.models.RetryCriteria;
import io.castled.jarvis.taskmanager.models.requests.TaskCreateRequest;
import io.castled.misc.PipelineScheduleManager;
import io.castled.models.*;
import io.castled.models.users.User;
import io.castled.pubsub.MessagePublisher;
import io.castled.pubsub.registry.PipelineUpdatedMessage;
import io.castled.resources.validators.ResourceAccessController;
import io.castled.schema.SchemaUtils;
import io.castled.schema.models.RecordSchema;
import io.castled.utils.JsonUtils;
import io.castled.utils.TimeUtils;
import io.castled.warehouses.WarehouseConnector;
import io.castled.warehouses.WarehouseService;
import io.castled.warehouses.WarehouseType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Singleton
@SuppressWarnings({"rawtypes", "unchecked"})
public class PipelineService {

    private final PipelineDAO pipelineDAO;
    private final PipelineCache pipelineCache;
    private final PipelineRunDAO pipelineRunDAO;
    private final CastledEventsClient castledEventsClient;
    private final Map<WarehouseType, WarehouseConnector> warehouseConnectors;
    private final Map<ExternalAppType, ExternalAppConnector> appConnectors;
    private final WarehouseService warehouseService;
    private final ExternalAppService externalAppService;
    private final ErrorReportsDAO errorReportsDAO;
    private final MessagePublisher messagePublisher;
    //just to make sure uuid starts with a character
    private static final String UUID_PREFIX = "c";
    private static final String ERROR_CODE = "__castled__error_code";
    private static final String ERROR_MESSAGE = "__castled__error_message";
    private static final String ERROR_RECORD_COUNT = "__castled__record_count";
    private final ResourceAccessController resourceAccessController;


    @Inject
    public PipelineService(Jdbi jdbi, CastledEventsClient castledEventsClient,
                           Map<WarehouseType, WarehouseConnector> warehouseConnectors,
                           WarehouseService warehouseService,
                           PipelineCache pipelineCache, ExternalAppService externalAppService,
                           ResourceAccessController resourceAccessController, MessagePublisher messagePublisher,
                           Map<ExternalAppType, ExternalAppConnector> appConnectors) {
        this.pipelineDAO = jdbi.onDemand(PipelineDAO.class);
        this.pipelineRunDAO = jdbi.onDemand(PipelineRunDAO.class);
        this.errorReportsDAO = jdbi.onDemand(ErrorReportsDAO.class);
        this.castledEventsClient = castledEventsClient;
        this.warehouseConnectors = warehouseConnectors;
        this.warehouseService = warehouseService;
        this.pipelineCache = pipelineCache;
        this.externalAppService = externalAppService;
        this.resourceAccessController = resourceAccessController;
        this.messagePublisher = messagePublisher;
        this.appConnectors = appConnectors;
    }

    public Long createPipeline(PipelineConfigDTO pipelineConfigDTO, User user) {

        try {
            ExternalApp externalApp = this.externalAppService.getExternalApp(pipelineConfigDTO.getAppId(), true);
            PipelineConfigDTO enrichedPipelineConfig = this.appConnectors.get(externalApp.getType()).validateAndEnrichPipelineConfig(pipelineConfigDTO);
            validPipelineConfig(enrichedPipelineConfig);
            Long pipelineId = this.pipelineDAO.createPipeline(enrichedPipelineConfig, user,
                    UUID_PREFIX + UUID.randomUUID().toString().replaceAll("-", "_"));
            this.castledEventsClient.publishPipelineEvent(new PipelineEvent(pipelineId, PipelineEventType.PIPELINE_CREATED));
            return pipelineId;
        } catch (ClientErrorException e) {
            log.warn("Create pipeline failed for app {} and warehouse {}", pipelineConfigDTO.getAppId(),
                    pipelineConfigDTO.getWarehouseId(), e);
            throw e;
        } catch (Exception e) {
            log.warn("Create pipeline failed for app {} and warehouse {}", pipelineConfigDTO.getAppId(),
                    pipelineConfigDTO.getWarehouseId(), e);
            throw new CastledRuntimeException(e);
        }
    }

    private void validPipelineConfig(PipelineConfigDTO pipelineConfig) throws BadRequestException {
        if (CollectionUtils.isEmpty(pipelineConfig.getMapping().getPrimaryKeys())) {
            throw new BadRequestException("Atleast one primary key needs to be selected for creating a pipeline");
        }

    }

    public void updatePipeline(Long pipelineId, PipelineUpdateRequest pipelineUpdateRequest) {
        this.pipelineDAO.updatePipeline(pipelineId, pipelineUpdateRequest.getName(), pipelineUpdateRequest.getSchedule());
    }

    public void triggerPipeline(long pipelineId, long teamId) {
        Pipeline pipeline = getActivePipeline(pipelineId);
        resourceAccessController.validatePipelineAccess(pipeline, teamId);
        doTriggerPipeline(pipeline);

    }

    private void doTriggerPipeline(Pipeline pipeline) {
        try {
            TaskCreateRequest taskCreateRequest = TaskCreateRequest.builder()
                    .group(JarvisTaskGroup.PIPELINE_RUN.name())
                    .type(JarvisTaskType.PIPELINE_RUN.name())
                    .expiry(Math.max(TimeUtils.secondsToMillis(pipeline.getJobSchedule().getExecutionTime()),
                            TimeUtils.minutesToMillis(120)))
                    .params(ImmutableMap.of(CommonConstants.PIPELINE_ID, pipeline.getId()))
                    .uniqueId(String.valueOf(pipeline.getId())).retryCriteria(new RetryCriteria(3, true))
                    .build();
            ObjectRegistry.getInstance(JarvisTasksClient.class).createTask(taskCreateRequest);
        } catch (Exception e) {
            log.error("Trigger pipeline {} failed", pipeline.getId());
            throw new CastledRuntimeException(e);
        }

    }

    public void restartPipeline(Long pipelineId, Long teamId) throws Exception {
        Pipeline pipeline = getActivePipeline(pipelineId, true);
        this.resourceAccessController.validatePipelineAccess(pipeline, teamId);
        Warehouse warehouse = warehouseService.getWarehouse(pipeline.getWarehouseId(), true);
        this.warehouseConnectors.get(warehouse.getType()).restartPoll(pipeline.getUuid(), warehouse.getConfig());
        doTriggerPipeline(pipeline);
    }

    public Pipeline getActivePipeline(Long pipelineId, boolean cached) {
        if (cached) {
            return pipelineCache.getValue(pipelineId);
        }
        return this.pipelineDAO.getActivePipeline(pipelineId);
    }

    public void updatePipelineRunstage(Long pipelineRunId, PipelineRunStage stage) {
        this.pipelineRunDAO.updatePipelineRunStage(pipelineRunId, stage);
    }

    public void deletePipeline(Long pipelineId, Long teamId) {
        Pipeline pipeline = getActivePipeline(pipelineId, true);
        this.resourceAccessController.validatePipelineAccess(pipeline, teamId);
        this.pipelineDAO.markPipelineDeleted(pipelineId);
        this.castledEventsClient.publishPipelineEvent(new PipelineEvent(pipelineId, PipelineEventType.PIPELINE_DELETED));
    }

    public void pausePipeline(long pipelineId, Long teamId) {
        Pipeline pipeline = getActivePipeline(pipelineId, true);
        this.resourceAccessController.validatePipelineAccess(pipeline, teamId);
        ObjectRegistry.getInstance(PipelineScheduleManager.class).unschedulePipeline(pipelineId);
        this.pipelineDAO.updateSyncStatus(pipelineId, PipelineSyncStatus.PAUSED);
        this.messagePublisher.publishMessage(new PipelineUpdatedMessage(pipelineId));
    }

    public void resumePipeline(long pipelineId, Long teamId) {
        Pipeline pipeline = getActivePipeline(pipelineId, true);
        this.resourceAccessController.validatePipelineAccess(pipeline, teamId);
        ObjectRegistry.getInstance(PipelineScheduleManager.class).reschedulePipeline(pipelineId);
        this.pipelineDAO.updateSyncStatus(pipelineId, PipelineSyncStatus.ACTIVE);
        this.messagePublisher.publishMessage(new PipelineUpdatedMessage(pipelineId));
    }

    public Pipeline getActivePipeline(Long pipelineId) {
        return getActivePipeline(pipelineId, false);
    }

    public long createPipelineRun(Long pipelineId) {
        return this.pipelineRunDAO.createPipelineRun(pipelineId,
                new PipelineSyncStats(0, 0, 0, 0));
    }

    public void markPipelineRunProcessed(Long pipelineId, PipelineSyncStats pipelineSyncStats) {
        this.pipelineRunDAO.markProcessed(pipelineId, pipelineSyncStats);
    }

    public void markPipelineRunFailed(Long pipelineId, String failureMessage) {
        this.pipelineRunDAO.markFailed(pipelineId, failureMessage);
    }

    public void updateSyncStats(Long pipelineRunId, PipelineSyncStats pipelineSyncStats) {
        this.pipelineRunDAO.updateSyncStatus(pipelineRunId, pipelineSyncStats);
    }

    public PipelineRun getPipelineRun(Long pipelineRunId) {
        return pipelineRunDAO.getPipelineRun(pipelineRunId);
    }

    public List<PipelineRun> getPipelineRuns(Long pipelineId, int limit) {
        if (limit == 0) {
            return pipelineRunDAO.getLastPipelineRuns(pipelineId, 100);
        }
        return pipelineRunDAO.getLastPipelineRuns(pipelineId, limit);
    }

    public PipelineRun getLastRun(Long pipelineId) {
        List<PipelineRun> pipelineRuns = getPipelineRuns(pipelineId, 1);
        if (CollectionUtils.isEmpty(pipelineRuns)) {
            return null;
        }
        return pipelineRuns.get(0);
    }

    public PipelineRunErrors getPipelineRunErrors(Long pipelineRunId) {
        ErrorReport errorReport = errorReportsDAO.getErrorReport(pipelineRunId);
        if (errorReport == null) {
            return new PipelineRunErrors(Lists.newArrayList(), Lists.newArrayList());
        }

        List<PipelineErrorAndSample> pipelineErrorAndSamples = Lists.newArrayList();
        String[] reportTokens = errorReport.getReport().split(System.lineSeparator());
        List<String> sampleFields = JsonUtils.jsonStringToTypeReference(reportTokens[0], new TypeReference<List<String>>() {
        });
        for (String error : Arrays.asList(reportTokens).subList(1, reportTokens.length)) {
            pipelineErrorAndSamples.add(JsonUtils.jsonStringToObject(error, PipelineErrorAndSample.class));
        }
        return new PipelineRunErrors(sampleFields, pipelineErrorAndSamples);
    }

    public StreamingOutput downloadErrorReport(Long pipelineRunId) throws IOException {

        PipelineRunErrors pipelineRunErrors = getPipelineRunErrors(pipelineRunId);
        List<String> errorReportFields = getErrorReportFields(pipelineRunErrors.getSampleFields());
        StringBuffer errorBuffer = new StringBuffer();


        CSVPrinter csvPrinter = new CSVPrinter(errorBuffer, CSVFormat.DEFAULT
                .withHeader(errorReportFields.toArray(new String[0])).withQuoteMode(QuoteMode.ALL));
        for (PipelineErrorAndSample pipelineErrorAndSample : pipelineRunErrors.getErrorAndSamples()) {
            List<String> fieldValues = Lists.newArrayList();
            for (String sampleField : pipelineRunErrors.getSampleFields()) {
                fieldValues.add(Optional.ofNullable(pipelineErrorAndSample.getRecord().get(sampleField))
                        .map(Object::toString).orElse("N/A"));

            }
            fieldValues.add(pipelineErrorAndSample.getErrorCode().name());
            fieldValues.add(pipelineErrorAndSample.getDescription());
            fieldValues.add(String.valueOf(pipelineErrorAndSample.getRecordCount()));
            csvPrinter.printRecord(fieldValues);
        }
        csvPrinter.flush();
        csvPrinter.close();

        return outputStream -> {
            outputStream.write(errorBuffer.toString().getBytes());
            outputStream.flush();
        };
    }

    public Pipeline getPipeline(Long pipelineId) {
        return pipelineDAO.getPipeline(pipelineId);
    }

    private List<String> getErrorReportFields(List<String> sampleFields) {
        List<String> reportFields = Lists.newArrayList(sampleFields);
        reportFields.add(ERROR_CODE);
        reportFields.add(ERROR_MESSAGE);
        reportFields.add(ERROR_RECORD_COUNT);
        return reportFields;
    }

    public void triggerDummyRun() {
        try {
            TaskCreateRequest taskCreateRequest = TaskCreateRequest.builder()
                    .type(JarvisTaskType.DUMMY.name())
                    .group(JarvisTaskGroup.OTHERS.name())
                    .expiry(TimeUtils.minutesToMillis(1345))
                    .retryCriteria(new RetryCriteria(3, true))
                    .build();
            ObjectRegistry.getInstance(JarvisTasksClient.class).createTask(taskCreateRequest);
        } catch (Exception e) {
            log.error("Trigger dummy run failed", e);
            throw new CastledRuntimeException(e);
        }
    }

    public PipelineSchema getPipelineSchema(AppSyncConfigDTO appSyncConfigDTO) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(2,
                new ThreadFactoryBuilder().setNameFormat("pipeline-schema-fetch-%d").build());
        try {
            Future<RecordSchema> warehouseSchema = executorService.submit(() -> warehouseService.fetchSchema(appSyncConfigDTO.getWarehouseId(), appSyncConfigDTO.getSourceQuery()));
            Future<ExternalAppSchema> externalAppSchema = executorService.submit(() -> externalAppService.getObjectSchema(appSyncConfigDTO.getAppId(), appSyncConfigDTO.getAppSyncConfig()));

            return new PipelineSchema(SchemaUtils.transformToSimpleSchema(enrichWarehouseSchema(appSyncConfigDTO, warehouseSchema)),
                    SchemaUtils.transformToSimpleSchema(externalAppSchema.get().getAppSchema()),
                    externalAppSchema.get().getPkEligibles());
        } finally {
            executorService.shutdownNow();
        }
    }

    private RecordSchema enrichWarehouseSchema(AppSyncConfigDTO appSyncConfigDTO, Future<RecordSchema> warehouseSchema) throws InterruptedException, java.util.concurrent.ExecutionException {
        ExternalApp externalApp = this.externalAppService.getExternalApp(appSyncConfigDTO.getAppId(), true);
        RecordSchema warehouseASchema = this.appConnectors.get(externalApp.getType()).enrichWarehouseASchema(appSyncConfigDTO, warehouseSchema.get());
        return warehouseASchema;
    }

    public List<Pipeline> listPipelines(Long teamid, Long appId) {
        if (appId == null) {
            return pipelineDAO.listPipelines(teamid);
        }
        return pipelineDAO.listPipelines(teamid, appId);
    }

    public List<WarehouseAggregate> getWarehouseAggregates(Long teamId) {
        return pipelineDAO.aggregateByWarehouse(teamId);
    }

    public int getWarehousePipelines(Long warehouseId) {
        return pipelineDAO.getWarehousePipelines(warehouseId);
    }

    public int getAppPipelines(Long appId) {
        return pipelineDAO.getAppPipelines(appId);
    }

    public List<AppAggregate> getAppAggregates(Long teamId) {
        return pipelineDAO.aggregateByApp(teamId);
    }
}
