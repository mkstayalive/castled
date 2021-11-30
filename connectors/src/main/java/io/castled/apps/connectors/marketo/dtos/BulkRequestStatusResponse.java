package io.castled.apps.connectors.marketo.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkRequestStatusResponse {

    private String requestId;
    private Boolean success;
    private List<BulkSyncRequestStatus> result;
    private List<ErrorResponse> errors;
}
