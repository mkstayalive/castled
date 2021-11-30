package io.castled.warehouses;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.caches.WarehouseCache;
import io.castled.constants.CommonConstants;
import io.castled.daos.PipelineDAO;
import io.castled.encryption.EncryptionManager;
import io.castled.exceptions.CastledException;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.exceptions.connect.ConnectException;
import io.castled.forms.dtos.FormFieldsDTO;
import io.castled.jarvis.JarvisTaskGroup;
import io.castled.jarvis.JarvisTaskType;
import io.castled.jarvis.taskmanager.JarvisTasksClient;
import io.castled.jarvis.taskmanager.models.RetryCriteria;
import io.castled.jarvis.taskmanager.models.Task;
import io.castled.jarvis.taskmanager.models.TaskStatus;
import io.castled.jarvis.taskmanager.models.requests.TaskCreateRequest;
import io.castled.models.*;
import io.castled.models.users.User;
import io.castled.pubsub.MessagePublisher;
import io.castled.pubsub.registry.WarehouseUpdatedMessage;
import io.castled.resources.validators.ResourceAccessController;
import io.castled.schema.models.RecordSchema;
import io.castled.utils.JsonUtils;
import io.castled.utils.TimeUtils;
import io.castled.warehouses.dtos.WarehouseAttributes;
import io.castled.warehouses.dtos.WarehouseTypeDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.BadRequestException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Singleton
@SuppressWarnings({"rawtypes", "unchecked"})
public class WarehouseService {

    private final Map<WarehouseType, WarehouseConnector> warehouseConnectors;
    private final EncryptionManager encryptionManager;
    private final WarehouseDAO warehouseDAO;
    private final PipelineDAO pipelineDAO;
    private final WarehouseCache warehouseCache;
    private final MessagePublisher messagePublisher;
    private final ResourceAccessController accessController;

    @Inject
    public WarehouseService(Map<WarehouseType, WarehouseConnector> warehouseConnectors,
                            EncryptionManager encryptionManager, Jdbi jdbi, WarehouseCache warehouseCache,
                            MessagePublisher messagePublisher, ResourceAccessController accessController) {
        this.warehouseConnectors = warehouseConnectors;
        this.encryptionManager = encryptionManager;
        this.warehouseDAO = jdbi.onDemand(WarehouseDAO.class);
        this.pipelineDAO = jdbi.onDemand(PipelineDAO.class);
        this.warehouseCache = warehouseCache;
        this.messagePublisher = messagePublisher;
        this.accessController = accessController;
    }

    public void testConnection(WarehouseType warehouseType, WarehouseConfig warehouseConfig) throws ConnectException {
        try {
            this.warehouseConnectors.get(warehouseType).testConnectionForDataPoll(warehouseConfig);
        } catch (ConnectException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public Long createWarehouse(WarehouseAttributes warehouseAttributes, User user) {

        WarehouseConfig warehouseConfig = warehouseAttributes.getConfig();
        try {
            testConnection(warehouseConfig.getType(), warehouseConfig);
            String config = this.encryptionManager.encryptText(JsonUtils.objectToString(warehouseConfig), user.getTeamId());
            return warehouseDAO.createWarehouse(warehouseAttributes.getName(), warehouseConfig.getType(), config, user.getTeamId());
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Create warehouse request failed for team {} and type {}", user.getTeamId(), warehouseConfig.getType(), e);
            throw new CastledRuntimeException(e);
        }
    }

    public FormFieldsDTO getFormFields(WarehouseType warehouseType) {
        return this.warehouseConnectors.get(warehouseType).getFormFields();
    }

    public void updateWarehouse(Long warehouseId, WarehouseAttributes warehouseAttributes, User user) throws CastledException {
        try {

            Warehouse warehouse = getWarehouse(warehouseId, false);
            accessController.validateWarehouseAccess(warehouse, user.getTeamId());

            WarehouseConfig warehouseConfig = warehouseAttributes.getConfig();
            testConnection(warehouseAttributes.getConfig().getType(), warehouseConfig);
            String config = this.encryptionManager.encryptText(JsonUtils.objectToString(warehouseConfig), user.getTeamId());
            warehouseDAO.updateWarehouse(warehouseId, warehouseAttributes.getName(), config);
            this.messagePublisher.publishMessage(new WarehouseUpdatedMessage(warehouseId));
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Update warehouse request failed for id {}", warehouseId, e);
            if (e instanceof ConnectException) {
                throw new BadRequestException(e.getMessage());
            }
            throw new CastledException(e.getMessage());
        }
    }

    public Warehouse getWarehouse(Long warehouseId) {
        return getWarehouse(warehouseId, false);
    }

    public void deleteWarehouse(Long warehouseId, Long teamId) {
        Warehouse warehouse = getWarehouse(warehouseId, true);
        accessController.validateWarehouseAccess(warehouse, teamId);
        if (this.pipelineDAO.getPipelinesByWhId(warehouseId).size() > 0) {
            throw new BadRequestException("Please delete all pipelines corresponding to the warehouse before deleting it");
        }
        warehouseDAO.deleteWarehouse(warehouseId);
    }

    public List<Warehouse> getAllWarehouses(WarehouseType warehouseType, Long teamId) {
        return this.warehouseDAO.listWarehouses(teamId).stream().filter(warehouse -> (warehouseType == null || warehouse.getType() == warehouseType))
                .collect(Collectors.toList());
    }

    public Warehouse getWarehouse(Long warehouseId, boolean cached) {
        if (cached) {
            return warehouseCache.getValue(warehouseId);
        }
        return this.warehouseDAO.getWarehouse(warehouseId);
    }

    public void updateWarehouseConfig(Long warehouseId, WarehouseConfig warehouseConfig, long teamId) {
        try {
            Warehouse warehouse = getWarehouse(warehouseId, false);
            accessController.validateWarehouseAccess(warehouse, teamId);
            String configStr = this.encryptionManager.encryptText(JsonUtils.objectToString(warehouseConfig), warehouse.getTeamId());
            warehouseDAO.updateWarehouseConfig(warehouseId, configStr);
            this.messagePublisher.publishMessage(new WarehouseUpdatedMessage(warehouseId));
        } catch (Exception e) {
            log.error("Update warehouse config failed for warehouse {}", warehouseId);
            throw new CastledRuntimeException(e);
        }
    }

    public RecordSchema fetchSchema(Long warehouseId, String query) throws Exception {
        Warehouse warehouse = getWarehouse(warehouseId);
        return warehouseConnectors.get(warehouse.getType()).getQuerySchema(warehouse.getConfig(), query);
    }

    public QueryId previewQuery(Long warehouseId, String query, long teamId) throws Exception {

        Warehouse warehouse = getWarehouse(warehouseId, true);
        this.accessController.validateWarehouseAccess(warehouse, teamId);
        String queryId = UUID.randomUUID().toString();
        TaskCreateRequest taskCreateRequest = TaskCreateRequest.builder()
                .group(JarvisTaskGroup.OTHERS.name())
                .type(JarvisTaskType.PREVIEW_QUERY.name())
                .expiry(TimeUtils.minutesToMillis(60))
                .params(ImmutableMap.of(CommonConstants.WAREHOUSE_ID, warehouseId,
                        CommonConstants.QUERY, query))
                .searchId(queryId).retryCriteria(new RetryCriteria(3, true))
                .build();
        ObjectRegistry.getInstance(JarvisTasksClient.class).createTaskSync(taskCreateRequest);
        return new QueryId(queryId);
    }

    public QueryStatusAndResults getQueryResults(String queryId) throws Exception {
        //there can be only one task with one search id logically
        List<Task> tasks = ObjectRegistry.getInstance(JarvisTasksClient.class).getTasksBySearchId(queryId, JarvisTaskType.PREVIEW_QUERY.name());
        if (CollectionUtils.isEmpty(tasks)) {
            throw new BadRequestException(String.format("Query id %s not found", queryId));
        }
        if (tasks.size() > 1) {
            throw new BadRequestException(String.format("Multiple queries with the same query id %s found", queryId));
        }
        Task task = tasks.get(0);
        if (task.getStatus() == TaskStatus.FAILED) {
            return QueryStatusAndResults.builder().status(QueryStatus.FAILED).failureMessage(task.getFailureMessage()).build();
        }
        if (task.getStatus() == TaskStatus.PROCESSED) {
            QueryResults queryResults = JsonUtils.jsonStringToObject(task.getResult(), QueryResults.class);
            return QueryStatusAndResults.builder().status(QueryStatus.SUCCEEDED).queryResults(queryResults).build();
        }
        return QueryStatusAndResults.builder().status(QueryStatus.PENDING).build();
    }

    public List<WarehouseTypeDTO> listWarehouseTypes(User user) {
        List<Warehouse> warehouses = this.warehouseDAO.listWarehouses(user.getTeamId());
        return Arrays.stream(WarehouseType.values()).map(warehouseType -> new WarehouseTypeDTO(warehouseType, warehouseType.title(),
                        warehouseType.getAccessType(), warehouseType.getLogoUrl(), warehouseType.getDocUrl(),
                        warehouses.stream().filter(warehouse -> warehouse.getType().equals(warehouseType)).count()))
                .collect(Collectors.toList());
    }
}
