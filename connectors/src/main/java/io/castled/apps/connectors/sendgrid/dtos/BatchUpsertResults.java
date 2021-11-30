package io.castled.apps.connectors.sendgrid.dtos;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BatchUpsertResults {

    private Integer requestedCount;
    private Integer createdCount;
    private Integer updatedCount;
    private Integer deletedCount;
    private Integer erroredCount;
    private String errorsUrl;
}
