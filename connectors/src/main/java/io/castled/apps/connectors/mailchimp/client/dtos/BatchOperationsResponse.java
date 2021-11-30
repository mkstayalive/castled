package io.castled.apps.connectors.mailchimp.client.dtos;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BatchOperationsResponse {
    private String id;
    private BatchOperationStatus status;
    private long totalOperations;
    private long finishedOperations;
    private long erroredOperations;
    private String responseBodyUrl;
}
