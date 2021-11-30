package io.castled.jarvis.taskmanager.scheduledjobs;

import com.google.inject.Inject;
import io.castled.jarvis.scheduler.JarvisGlobalCronJob;
import io.castled.jarvis.taskmanager.JarvisTasksClient;
import io.castled.jarvis.taskmanager.JarvisTasksService;
import io.castled.jarvis.taskmanager.daos.JarvisTasksDAO;
import io.castled.jarvis.taskmanager.models.Task;
import io.castled.jarvis.taskmanager.models.TaskStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.JobExecutionContext;

import java.util.List;

public class JarvisTaskRetryJob implements JarvisGlobalCronJob {
    private final JarvisTasksService jarvisTasksService;
    private final JarvisTasksDAO jarvisTasksDAO;

    @Inject
    public JarvisTaskRetryJob(JarvisTasksClient jarvisTasksClient) {
        this.jarvisTasksService = jarvisTasksClient.getJarvisTasksService();
        this.jarvisTasksDAO = this.jarvisTasksService.getJarvisTasksDAO();
    }

    private void processRetriableTasks() {
        while (true) {
            List<Task> retriableTasks = this.jarvisTasksDAO.getTasksInStatus(TaskStatus.FAILED_TEMPORARILY, 1000);
            if (CollectionUtils.isEmpty(retriableTasks)) {
                break;
            }
            this.jarvisTasksService.reEnqueueTasks(retriableTasks, true);
        }
    }

    private void retryDeferredTasks() {
        while (true) {
            List<Task> retriableTasks = this.jarvisTasksDAO.getRetriableDeferredTasks(1000);
            if (CollectionUtils.isEmpty(retriableTasks)) {
                break;
            }
            this.jarvisTasksService.reEnqueueTasks(retriableTasks, true);
        }
    }

    @Override
    public void execute(JobExecutionContext context) {
        processRetriableTasks();
        retryDeferredTasks();
    }
}
