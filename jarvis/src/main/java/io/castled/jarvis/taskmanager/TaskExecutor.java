package io.castled.jarvis.taskmanager;

import io.castled.jarvis.taskmanager.models.Task;

public interface TaskExecutor {

    String executeTask(Task task) throws Exception;
}
