package io.castled.jarvis.taskmanager;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.castled.jarvis.taskmanager.daos.JarvisTasksDAO;
import io.castled.jarvis.taskmanager.exceptions.JarvisDeferredException;
import io.castled.jarvis.taskmanager.exceptions.JarvisRetriableException;
import io.castled.jarvis.taskmanager.models.Task;
import io.castled.jarvis.taskmanager.models.TaskStatus;
import io.castled.jarvis.taskmanager.models.requests.TaskDeferralRequest;
import io.castled.jarvis.taskmanager.models.requests.TaskFailureRequest;
import io.castled.jarvis.taskmanager.models.requests.TaskSuccessRequest;
import io.castled.kafka.producer.CastledKafkaProducer;
import io.castled.utils.JsonUtils;
import io.castled.utils.ThreadUtils;
import io.castled.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import net.greghaines.jesque.Job;
import net.greghaines.jesque.worker.JobFactory;
import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
public class JarvisJobFactory implements JobFactory {

    private final Map<String, TaskExecutor> taskExecutors;
    private final JarvisTasksDAO jarvisTasksDAO;
    private final CastledKafkaProducer kafkaProducer;

    private class JarvisTaskExecutor {

        private final TaskExecutor taskExecutor;

        public JarvisTaskExecutor(TaskExecutor taskExecutor) {
            this.taskExecutor = taskExecutor;
        }

        public void executeTask(Task task) {
            try {
                String taskResult = taskExecutor.executeTask(task);
                handleTaskSuccess(task.getId(), taskResult);
            } catch (Exception e) {
                handleTaskFailure(e, task);
            }
        }

        public void handleTaskSuccess(Long taskId, String taskResult) {
            try {
                TaskSuccessRequest taskSuccessRequest = new TaskSuccessRequest(taskId, taskResult);
                ProducerRecord<byte[], byte[]> producerRecord = new ProducerRecord<>(JarvisConstants.JARVIS_EVENTS_TOPIC,
                        JsonUtils.objectToByteArray(taskSuccessRequest));
                kafkaProducer.publishSync(producerRecord);
            } catch (Exception e) {
                try {
                    jarvisTasksDAO.markTaskProcessed(taskId, taskResult);
                } catch (Exception e1) {
                    log.error("Publish task success request failed for task {}", taskId);
                }
            }
        }

        private void handleTaskFailure(Exception e, Task task) {
            if (e instanceof JarvisRetriableException) {
                if (task.getAttempts() <= task.getRetryCriteria().getMaxRetries()) {
                    publishTaskFailure(task.getId(), TaskStatus.FAILED_TEMPORARILY, e.getMessage(), task.getAttempts() + 1);
                } else {
                    publishTaskFailure(task.getId(), TaskStatus.FAILED, e.getMessage(), task.getAttempts() + 1);
                }
            } else if (e instanceof JarvisDeferredException) {
                publishTaskDeferral(task.getId(), ((JarvisDeferredException) e).getDeferredTill());
            } else {
                log.error("Jarvis task execution failed for task {}", task.getId(), e);
                publishTaskFailure(task.getId(), TaskStatus.FAILED, e.getMessage(), task.getAttempts() + 1);
            }
        }
    }

    private void publishTaskFailure(Long taskId, TaskStatus taskStatus, String failureMessage, int attempts) {
        try {
            failureMessage = Optional.ofNullable(failureMessage).orElse("Unknown Error");
            TaskFailureRequest taskFailureRequest = new TaskFailureRequest(taskId, taskStatus, failureMessage, attempts);
            ProducerRecord<byte[], byte[]> producerRecord = new ProducerRecord<>(JarvisConstants.JARVIS_EVENTS_TOPIC,
                    JsonUtils.objectToByteArray(taskFailureRequest));
            kafkaProducer.publishSync(producerRecord);
        } catch (Exception e) {
            try {
                jarvisTasksDAO.markFailed(taskId, taskStatus, failureMessage, attempts);
            } catch (Exception e1) {
                log.error("Publish task failure request failed for task {}", taskId);
            }
        }
    }

    private void publishTaskDeferral(Long taskId, Long deferredTill) {
        try {
            TaskDeferralRequest taskDeferralRequest = new TaskDeferralRequest(taskId, deferredTill);
            ProducerRecord<byte[], byte[]> producerRecord = new ProducerRecord<>(JarvisConstants.JARVIS_EVENTS_TOPIC,
                    JsonUtils.objectToByteArray(taskDeferralRequest));
            kafkaProducer.publishSync(producerRecord);
        } catch (Exception e) {
            try {
                jarvisTasksDAO.markDeferred(taskId, new Timestamp(System.currentTimeMillis() + deferredTill));
            } catch (Exception e1) {
                log.error("Publish task deferral request failed for task {}", taskId);
            }
        }
    }

    public JarvisJobFactory(Map<String, TaskExecutor> taskExecutors, JarvisTasksDAO jarvisTasksDAO,
                            CastledKafkaProducer kafkaProducer) {
        this.taskExecutors = taskExecutors;
        this.jarvisTasksDAO = jarvisTasksDAO;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public Object materializeJob(Job job) throws Exception {
        Long taskId = MapUtils.getLong(job.getVars(), JarvisConstants.ID_FIELD);
        Task task = this.jarvisTasksDAO.getTask(taskId);
        if (TaskStatus.terminalStates().contains(task.getStatus())) {
            // task is already in terminal state
            return null;
        }
        this.jarvisTasksDAO.updateTaskStatus(Collections.singletonList(taskId), TaskStatus.PICKED);
        ExecutorService executorService = Executors.newSingleThreadExecutor
                (new ThreadFactoryBuilder().setNameFormat("jarvis-task-executor-%d").build());
        Future<?> result = executorService.submit(() -> new JarvisTaskExecutor(taskExecutors.get(task.getType())).executeTask(task));
        try {
            Object value = result.get(Math.max(task.getExpiry(), TimeUtils.hoursToMillis(1)), TimeUnit.MILLISECONDS);
            ThreadUtils.terminateGracefully(executorService, 1);
            return value;
        } catch (TimeoutException e) {
            int attempts = task.getAttempts() + 1;
            if (task.getRetryCriteria().isRetryOnExpiry() && attempts <= task.getRetryCriteria().getMaxRetries()) {
                publishTaskFailure(taskId, TaskStatus.FAILED_TEMPORARILY, "Task Expired", attempts);
            } else {
                publishTaskFailure(taskId, TaskStatus.FAILED, "Task Expired", attempts);
            }
        } catch (InterruptedException e) {
            ThreadUtils.terminateGracefully(executorService, 10);
            jarvisTasksDAO.markFailed(taskId, TaskStatus.FAILED_TEMPORARILY, "Task Interrupted", task.getAttempts());
        }
        return null;
    }
}
