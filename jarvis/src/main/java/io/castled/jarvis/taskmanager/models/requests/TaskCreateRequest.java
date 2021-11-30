package io.castled.jarvis.taskmanager.models.requests;

import io.castled.jarvis.taskmanager.models.RetryCriteria;
import io.castled.jarvis.taskmanager.models.TaskPriority;
import io.castled.utils.JsonUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Optional;

@Getter
@Setter
public class TaskCreateRequest extends JarvisRequest {

    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;
    private String uniqueId;
    private String group;
    private String type;
    private Long expiry;
    private String params;
    private String searchId;

    private RetryCriteria retryCriteria = new RetryCriteria();

    public TaskCreateRequest() {
        super(JarvisRequestType.TASK_CREATE);
    }

    @Builder
    public TaskCreateRequest(TaskPriority priority, String uniqueId, String group, String type, Long expiry,
                             RetryCriteria retryCriteria, Map<String, Object> params, String searchId) {
        super(JarvisRequestType.TASK_CREATE);
        this.priority = priority == null ? TaskPriority.MEDIUM : priority;
        this.uniqueId = uniqueId;
        this.group = group;
        this.type = type;
        this.expiry = expiry;
        this.retryCriteria = Optional.ofNullable(retryCriteria).orElse(new RetryCriteria());
        this.params = JsonUtils.objectToString(params);
        this.searchId = searchId;
    }
}
