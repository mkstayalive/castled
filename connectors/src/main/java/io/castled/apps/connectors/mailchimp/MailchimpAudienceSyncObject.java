package io.castled.apps.connectors.mailchimp;

import io.castled.apps.ExternalAppType;
import io.castled.apps.models.SyncObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class MailchimpAudienceSyncObject extends SyncObject {
    private String audienceId;

    public MailchimpAudienceSyncObject(String audienceId, String audienceName) {
        super(audienceName, ExternalAppType.MAILCHIMP);
        this.audienceId = audienceId;
    }
}
