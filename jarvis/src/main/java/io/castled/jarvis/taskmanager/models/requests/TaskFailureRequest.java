package io.castled.jarvis.taskmanager.models.requests;

import io.castled.jarvis.taskmanager.models.TaskStatus;
import lombok.Getter;

@Getter
public class TaskFailureRequest extends JarvisRequest {
    private Long taskId;
    private TaskStatus taskStatus;
    private String failureMessage;
    private int attempts;

    public TaskFailureRequest() {
        super(JarvisRequestType.TASK_FAILURE);
    }

    public TaskFailureRequest(Long taskId, TaskStatus taskStatus, String failureMessage, int attempts) {
        super(JarvisRequestType.TASK_FAILURE);
        this.taskId = taskId;
        this.failureMessage = failureMessage;
        this.taskStatus = taskStatus;
        this.attempts = attempts;
    }
}
