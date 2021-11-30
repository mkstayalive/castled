package io.castled.apps.connectors.sendgrid.dtos;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BatchUpsertStatusResponse {

    private String id;
    private String status;
    private String jobType;
    private BatchUpsertResults results;
    private String startedAt;
    private String finishedAt;
}
