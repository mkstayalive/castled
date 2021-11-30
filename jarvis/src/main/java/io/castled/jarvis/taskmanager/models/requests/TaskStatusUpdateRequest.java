package io.castled.jarvis.taskmanager.models.requests;

import io.castled.jarvis.taskmanager.models.TaskStatus;
import lombok.Getter;

@Getter
public class TaskStatusUpdateRequest extends JarvisRequest {

    private Long taskId;
    private TaskStatus taskStatus;
    private String taskResult;

    public TaskStatusUpdateRequest() {
        super(JarvisRequestType.TASK_STATUS_UPDATE);
    }

    public TaskStatusUpdateRequest(Long taskId, TaskStatus taskStatus, String taskResult) {
        super(JarvisRequestType.TASK_STATUS_UPDATE);
        this.taskId = taskId;
        this.taskStatus = taskStatus;
        this.taskResult = taskResult;
    }
}
