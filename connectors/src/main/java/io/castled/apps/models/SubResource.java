package io.castled.apps.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.castled.apps.ExternalAppType;
import io.castled.apps.connectors.googleads.GadsSubResource;
import lombok.*;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "appType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GadsSubResource.class, name = "GOOGLEADS")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubResource {

    private String objectName;
    private ExternalAppType appType;
}
