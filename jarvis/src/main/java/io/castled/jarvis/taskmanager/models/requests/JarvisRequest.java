package io.castled.jarvis.taskmanager.models.requests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "requestType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TaskCreateRequest.class, name = "TASK_CREATE"),
        @JsonSubTypes.Type(value = TaskStatusUpdateRequest.class, name = "TASK_STATUS_UPDATE"),
        @JsonSubTypes.Type(value = TaskDeferralRequest.class, name = "TASK_DEFERRAL"),
        @JsonSubTypes.Type(value = TaskSuccessRequest.class, name = "TASK_SUCCESS"),
        @JsonSubTypes.Type(value = TaskFailureRequest.class, name = "TASK_FAILURE")})
@AllArgsConstructor
@Getter
public abstract class JarvisRequest {
    private final JarvisRequestType requestType;
}
