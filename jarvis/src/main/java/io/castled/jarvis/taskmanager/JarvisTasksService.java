package io.castled.jarvis.taskmanager;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.castled.jarvis.taskmanager.daos.JarvisTasksDAO;
import io.castled.jarvis.taskmanager.exceptions.JarvisException;
import io.castled.jarvis.taskmanager.exceptions.JarvisTaskInProgressException;
import io.castled.jarvis.taskmanager.models.JesqueTaskParams;
import io.castled.jarvis.taskmanager.models.Task;
import io.castled.jarvis.taskmanager.models.TaskPriority;
import io.castled.jarvis.taskmanager.models.TaskStatus;
import io.castled.jarvis.taskmanager.models.requests.TaskCreateRequest;
import io.castled.kafka.producer.CastledKafkaProducer;
import io.castled.utils.ThreadUtils;
import org.apache.commons.collections4.CollectionUtils;


import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JarvisTasksService implements AutoCloseable {

    private final JarvisTasksDAO jarvisTasksDAO;
    private final Map<String, JesqueTasksClient> jesqueClientPool;
    private final CastledKafkaProducer kafkaProducer;

    @Inject
    public JarvisTasksService(JarvisTasksDAO jarvisTasksDAO, CastledKafkaProducer kafkaProducer,
                              Map<String, JesqueTasksClient> jesqueClientPool) {
        this.jarvisTasksDAO = jarvisTasksDAO;
        this.jesqueClientPool = jesqueClientPool;
        this.kafkaProducer = kafkaProducer;
    }

    public void updateTaskStatus(Long taskId, TaskStatus taskStatus) {
        this.jarvisTasksDAO.updateTaskStatus(Collections.singletonList(taskId), taskStatus);
    }

    public void markTaskFailed(Long taskId, TaskStatus taskStatus, String failureMessage, int attempts) {
        this.jarvisTasksDAO.markFailed(taskId, taskStatus, failureMessage, attempts);
    }

    public void markTaskProcessed(Long taskId, String taskResult) {
        this.jarvisTasksDAO.markTaskProcessed(taskId, taskResult);
    }

    public void markTaskDeferred(Long taskId, Long deferredTill) {
        this.jarvisTasksDAO.markDeferred(taskId, new Timestamp(deferredTill));
    }

    public void createTask(TaskCreateRequest taskCreateRequest) throws JarvisException {
        if (taskCreateRequest.getUniqueId() != null && this.jarvisTasksDAO.getTasksCount(taskCreateRequest.getType(),
                taskCreateRequest.getUniqueId(), TaskStatus.inProcessStates()) > 0) {
            throw new JarvisTaskInProgressException(taskCreateRequest.getUniqueId());
        }
        long taskId = this.jarvisTasksDAO.createTask(taskCreateRequest);
        this.jesqueClientPool.get(taskCreateRequest.getGroup())
                .enqueueTask(new JesqueTaskParams(taskId, taskCreateRequest.getType(), taskCreateRequest.getPriority()));
    }

    public void reEnqueueTasks(List<Task> tasks, boolean markQueued) {
        Map<String, List<Task>> tasksPerGroup = tasks.stream().collect(Collectors.groupingBy(Task::getGroup));
        for (String group : tasksPerGroup.keySet()) {
            List<Task> groupTasks = tasksPerGroup.get(group);
            List<Long> taskIds = groupTasks.stream().map(Task::getId).collect(Collectors.toList());
            List<JesqueTaskParams> taskParams = groupTasks.stream()
                    .map(this::buildTaskParams).collect(Collectors.toList());
            this.jesqueClientPool.get(group).batchEnqueueTasks(taskParams);
            if (markQueued) {
                markTasksQueued(taskIds);
            }
        }
    }

    public void prioritiseTasks(List<Task> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        Map<String, List<Task>> tasksPerGroup = tasks.stream().collect(Collectors.groupingBy(Task::getGroup));
        Map<Long, TaskPriority> updatedPriorities = Maps.newHashMap();
        for (String group : tasksPerGroup.keySet()) {
            List<Task> groupTasks = tasksPerGroup.get(group);
            List<JesqueTaskParams> taskParamsList = groupTasks.stream()
                    .map(this::buildTaskParams).collect(Collectors.toList());
            for (JesqueTaskParams taskParams : taskParamsList) {
                TaskPriority updatedPriority = this.jesqueClientPool.get(group).prioritiseTask(taskParams);
                if (updatedPriority != null) {
                    updatedPriorities.put(taskParams.getTaskId(), updatedPriority);
                }
            }
        }
        this.jarvisTasksDAO.updateTaskPriority(updatedPriorities.keySet(), updatedPriorities.values());
    }

    private void markTasksQueued(List<Long> taskIds) {
        int attempts = 0;
        int MAX_RETRIES = 5;
        while (true) {
            try {
                this.jarvisTasksDAO.updateTaskStatus(taskIds, TaskStatus.QUEUED);
                break;
            } catch (Exception e) {
                attempts++;
                if (attempts > MAX_RETRIES) {
                    throw e;
                }
                ThreadUtils.interruptIgnoredSleep(5000);
            }
        }
    }

    public List<Task> getTasksBySearchId(String searchId, String taskType) {
        return this.jarvisTasksDAO.getTasksBySearchId(searchId, taskType);

    }

    private JesqueTaskParams buildTaskParams(Task task) {
        return new JesqueTaskParams(task.getId(), task.getType(), task.getPriority());
    }

    public JarvisTasksDAO getJarvisTasksDAO() {
        return jarvisTasksDAO;
    }

    @Override
    public void close() {
        for (JesqueTasksClient jesqueTasksClient : jesqueClientPool.values()) {
            jesqueTasksClient.close();
        }
    }
}
