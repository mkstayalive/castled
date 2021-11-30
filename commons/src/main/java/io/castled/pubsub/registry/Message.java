package io.castled.pubsub.registry;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.castled.pubsub.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = WarehouseUpdatedMessage.class, name = "WAREHOUSE_UPDATED"),
        @JsonSubTypes.Type(value = ExternalAppUpdatedMessage.class, name = "EXTERNAL_APP_UPDATED"),
        @JsonSubTypes.Type(value = OAuthDetailsUpdatedMessage.class, name = "OAUTH_DETAILS_UPDATED"),
        @JsonSubTypes.Type(value = UserUpdatedMessage.class, name = "USER_UPDATED"),
        @JsonSubTypes.Type(value = PipelineUpdatedMessage.class, name = "PIPELINE_UPDATED")})
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@NoArgsConstructor
@AllArgsConstructor
public abstract class Message {
    private MessageType type;
}
