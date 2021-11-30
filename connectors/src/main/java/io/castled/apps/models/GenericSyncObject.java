package io.castled.apps.models;

import io.castled.apps.ExternalAppType;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GenericSyncObject extends SyncObject {

    public GenericSyncObject(String objectName, ExternalAppType appType) {
        super(objectName, appType);
    }

}
