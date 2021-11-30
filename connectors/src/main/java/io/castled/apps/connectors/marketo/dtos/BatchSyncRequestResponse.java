package io.castled.apps.connectors.marketo.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchSyncRequestResponse {

    private String requestId;
    private List<BatchSyncResult> result;
    private List<ErrorResponse> errors;
    private Boolean success;
}
