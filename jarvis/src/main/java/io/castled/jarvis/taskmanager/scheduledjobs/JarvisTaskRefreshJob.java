package io.castled.jarvis.taskmanager.scheduledjobs;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.castled.jarvis.scheduler.JarvisGlobalCronJob;
import io.castled.jarvis.taskmanager.JarvisConstants;
import io.castled.jarvis.taskmanager.JarvisTasksClient;
import io.castled.jarvis.taskmanager.JarvisTasksService;
import io.castled.jarvis.taskmanager.models.JarvisTaskClientConfig;
import io.castled.jarvis.taskmanager.models.Task;
import io.castled.jarvis.taskmanager.models.TaskStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.JobExecutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JarvisTaskRefreshJob implements JarvisGlobalCronJob {

    private final JarvisTasksService jarvisTasksService;
    private final JarvisTaskClientConfig clientConfig;

    @Inject
    public JarvisTaskRefreshJob(JarvisTasksClient jarvisTasksClient, JarvisTaskClientConfig clientConfig) {
        this.jarvisTasksService = jarvisTasksClient.getJarvisTasksService();
        this.clientConfig = clientConfig;
    }

    @Override
    public void execute(JobExecutionContext context) {
        reprioritiseTasks();
        refreshAnomolousTasks();
    }

    private void refreshAnomolousTasks() {
        int limit = 1000;
        long taskOffset = 0;
        long failureCorrectionThreshold = clientConfig.getPriorityCoolDownMins() * 2L;
        while (true) {
            List<Task> tasks = this.jarvisTasksService.getJarvisTasksDAO()
                    .getUnrefreshedTasks(failureCorrectionThreshold, taskOffset, Lists.newArrayList(TaskStatus.PICKED, TaskStatus.QUEUED), limit);
            if (CollectionUtils.isEmpty(tasks)) {
                break;
            }
            this.jarvisTasksService.reEnqueueTasks(tasks, false);
            taskOffset = tasks.get(tasks.size() - 1).getId();
        }
    }

    private void reprioritiseTasks() {
        int limit = 1000;
        long taskOffset = 0;

        while (true) {
            List<Task> tasks = this.jarvisTasksService.getJarvisTasksDAO()
                    .getUnrefreshedTasks(clientConfig.getPriorityCoolDownMins(), taskOffset, Lists.newArrayList(TaskStatus.QUEUED), limit);
            if (CollectionUtils.isEmpty(tasks)) {
                break;
            }
            List<Task> tasksToPrioritise = tasks.stream()
                    .filter(task -> task.getPriority().getRank() < JarvisConstants.MAX_UPGRADE_PRIORITY.getRank()).collect(Collectors.toList());
            this.jarvisTasksService.prioritiseTasks(tasksToPrioritise);

            taskOffset = tasks.get(tasks.size() - 1).getId();
        }

    }
}
