package io.castled.jarvis.taskmanager.models.requests;

import lombok.Getter;

public class TaskSuccessRequest extends JarvisRequest {

    @Getter
    private Long taskId;

    @Getter
    private String taskResult;

    public TaskSuccessRequest() {
        super(JarvisRequestType.TASK_SUCCESS);
    }

    public TaskSuccessRequest(Long taskId, String taskResult) {
        super(JarvisRequestType.TASK_SUCCESS);
        this.taskId = taskId;
        this.taskResult = taskResult;
    }
}
