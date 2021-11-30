package io.castled.jarvis.taskmanager.models;

import io.castled.jarvis.taskmanager.TaskExecutor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskGroup {

    private String group;
    private int workerThreads;
    private Map<String, TaskExecutor> taskExecutors;
}
