package io.castled.apps.connectors.marketo.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkSyncRequestStatus {

    private String batchId;
    private String status;
    private Integer numOfLeadsProcessed;
    private Integer numOfRowsFailed;
    private Integer numOfRowsWithWarning;
    private String message;

    public final static String STATUS_COMPLETE = "Complete";
    public final static String STATUS_QUEUED = "Queued";
    public final static String STATUS_IMPORTING = "Importing";
    public final static String STATUS_FAILED = "Failed";
}
