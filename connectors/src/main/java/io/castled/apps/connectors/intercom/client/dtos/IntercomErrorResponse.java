package io.castled.apps.connectors.intercom.client.dtos;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class IntercomErrorResponse {
    private String requestId;
    private String type;
    private List<IntercomObjectError> errors;

    public IntercomObjectError getFirstError() {
        return errors.get(0);
    }
}
