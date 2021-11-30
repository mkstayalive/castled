package io.castled.apps.models;

import io.castled.apps.ExternalAppType;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GenericSubResource extends SubResource {

    public GenericSubResource(String objectName, ExternalAppType appType) {
        super(objectName, appType);
    }
}
