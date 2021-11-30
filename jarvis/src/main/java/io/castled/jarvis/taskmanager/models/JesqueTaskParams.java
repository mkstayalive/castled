package io.castled.jarvis.taskmanager.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JesqueTaskParams {
    private Long taskId;
    private String taskType;
    private TaskPriority taskPriority;
}
