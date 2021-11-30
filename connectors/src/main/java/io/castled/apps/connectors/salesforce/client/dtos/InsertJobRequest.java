package io.castled.apps.connectors.salesforce.client.dtos;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InsertJobRequest extends JobRequest {

    public InsertJobRequest(String object, ContentType contentType) {
        super(object, contentType, Operation.INSERT.getName(), LineEnding.CRLF);
    }
}
