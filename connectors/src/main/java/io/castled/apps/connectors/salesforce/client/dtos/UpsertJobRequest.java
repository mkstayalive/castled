package io.castled.apps.connectors.salesforce.client.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpsertJobRequest extends JobRequest {
    private String externalIdFieldName;

    public UpsertJobRequest(String object, ContentType contentType, String externalId) {
        super(object, contentType, Operation.UPSERT.getName(), LineEnding.CRLF);
        this.externalIdFieldName = externalId;
    }
}
