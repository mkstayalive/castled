package io.castled.apps.connectors.googleads;

import io.castled.apps.models.SubResource;
import io.castled.apps.ExternalAppType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class GadsSubResource extends SubResource {

    private String resourceName;

    private CustomerMatchType customerMatchType;

    public GadsSubResource(CustomerMatchType customerMatchType, String objectName, String resourceName) {
        super(objectName, ExternalAppType.GOOGLEADS);
        this.resourceName = resourceName;
        this.customerMatchType = customerMatchType;
    }


    public GadsSubResource(String objectName, String resourceName) {
        super(objectName, ExternalAppType.GOOGLEADS);
        this.resourceName = resourceName;
    }
}
