package io.castled.jarvis.taskmanager;

import io.castled.exceptions.CastledRuntimeException;
import io.castled.jarvis.taskmanager.exceptions.JarvisTaskInProgressException;
import io.castled.jarvis.taskmanager.models.JarvisKafkaConfig;
import io.castled.jarvis.taskmanager.models.requests.*;
import io.castled.kafka.consumer.BaseKafkaConsumer;
import io.castled.kafka.consumer.KafkaConsumerConfiguration;
import io.castled.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;

@Slf4j
public class JarvisRequestsConsumer extends BaseKafkaConsumer {

    private final JarvisTasksService jarvisTasksService;

    public JarvisRequestsConsumer(JarvisKafkaConfig jarvisKafkaConfig, JarvisTasksService jarvisTasksService) {
        super(KafkaConsumerConfiguration.builder()
                .bootstrapServers(jarvisKafkaConfig.getBootstrapServers())
                .consumerGroup(JarvisConstants.JARVIS_CONSUMER_GRP)
                .topic(JarvisConstants.JARVIS_EVENTS_TOPIC).retryOnUnhandledFailures(true).build());
        this.jarvisTasksService = jarvisTasksService;
    }

    @Override
    public long processRecords(List<ConsumerRecord<byte[], byte[]>> partitionRecords) {
        long lastProcessedOffset = -1;
        try {
            for (ConsumerRecord<byte[], byte[]> record : partitionRecords) {
                JarvisRequest jarvisRequest = JsonUtils.byteArrayToObject(record.value(), JarvisRequest.class);
                switch (jarvisRequest.getRequestType()) {
                    case TASK_CREATE:
                        createTask((TaskCreateRequest) jarvisRequest);
                        break;
                    case TASK_SUCCESS:
                        TaskSuccessRequest taskSuccessRequest = (TaskSuccessRequest) jarvisRequest;
                        jarvisTasksService.markTaskProcessed(taskSuccessRequest.getTaskId(), taskSuccessRequest.getTaskResult());
                        break;
                    case TASK_STATUS_UPDATE:
                        TaskStatusUpdateRequest statusUpdateRequest = (TaskStatusUpdateRequest) jarvisRequest;
                        jarvisTasksService.updateTaskStatus(statusUpdateRequest.getTaskId(), statusUpdateRequest.getTaskStatus());
                        break;
                    case TASK_FAILURE:
                        TaskFailureRequest taskFailureRequest = (TaskFailureRequest) jarvisRequest;
                        this.jarvisTasksService.markTaskFailed(taskFailureRequest.getTaskId(), taskFailureRequest.getTaskStatus(),
                                taskFailureRequest.getFailureMessage(), taskFailureRequest.getAttempts());
                        break;
                    case TASK_DEFERRAL:
                        TaskDeferralRequest taskDeferralRequest = (TaskDeferralRequest) jarvisRequest;
                        this.jarvisTasksService.markTaskDeferred(taskDeferralRequest.getTaskId(),
                                System.currentTimeMillis() + taskDeferralRequest.getDeferredTill());
                        break;
                    default:
                        throw new CastledRuntimeException(String.format("Unknown request type %s", jarvisRequest.getRequestType()));
                }
                lastProcessedOffset = record.offset();
            }
        } catch (Exception e) {
            log.error("Jarvis events consumption failed", e);
            return lastProcessedOffset;
        }
        return lastProcessedOffset;
    }

    private void createTask(TaskCreateRequest taskCreateRequest) throws Exception {
        try {
            jarvisTasksService.createTask(taskCreateRequest);
        } catch (JarvisTaskInProgressException e) {
            log.info(String.format("Task with unique id %s already in progress", taskCreateRequest.getUniqueId()));
        }
    }
}
