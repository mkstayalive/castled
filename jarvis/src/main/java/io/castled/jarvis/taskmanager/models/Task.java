package io.castled.jarvis.taskmanager.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.sql.Timestamp;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Builder
public class Task {

    private Long id;

    private TaskStatus status;

    @NonNull
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    private String uniqueId;

    @NonNull
    //not keeping it typed, as different client might have different pool of task categories and types
    private String group;

    @NonNull
    private String type;

    @NonNull
    private Long expiry;

    private int attempts;

    private Map<String, Object> params;

    @Builder.Default
    private RetryCriteria retryCriteria = new RetryCriteria();

    private Timestamp createdTs;

    private Timestamp refreshedTs;

    private String searchId;

    private String result;

    private String failureMessage;

}
