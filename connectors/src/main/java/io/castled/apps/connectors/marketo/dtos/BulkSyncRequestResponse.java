package io.castled.apps.connectors.marketo.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkSyncRequestResponse {

    private String requestId;
    private List<BulkResult> result;
    private List<ErrorResponse> errors;
    private Boolean success;
}
