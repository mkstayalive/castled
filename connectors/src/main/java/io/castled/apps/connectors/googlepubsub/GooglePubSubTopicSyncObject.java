package io.castled.apps.connectors.googlepubsub;

import io.castled.apps.ExternalAppType;
import io.castled.apps.models.SyncObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class GooglePubSubTopicSyncObject extends SyncObject {
    private String topicName;
    private String topicId;

    public GooglePubSubTopicSyncObject(String topicId, String topicName) {
        super(topicName, ExternalAppType.GOOGLEPUBSUB);
        this.topicId = topicId;
        this.topicName = topicName;
    }
}
