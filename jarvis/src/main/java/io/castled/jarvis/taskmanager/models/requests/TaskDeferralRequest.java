package io.castled.jarvis.taskmanager.models.requests;

import lombok.Getter;

@Getter
public class TaskDeferralRequest extends JarvisRequest {
    private Long taskId;
    private Long deferredTill;

    public TaskDeferralRequest(Long taskId, Long deferredTill) {
        super(JarvisRequestType.TASK_DEFERRAL);
        this.taskId = taskId;
        this.deferredTill = deferredTill;
    }

    public TaskDeferralRequest() {
        super(JarvisRequestType.TASK_DEFERRAL);
    }
}
