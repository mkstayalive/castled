package io.castled.apps.connectors.activecampaign.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
//@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
//@AllArgsConstructor
@Getter
@Setter
public class BulkImportResponse {

    private Integer success;
    private Integer queued_contacts;
    private String batchId;
    private String message;

    private List<FailureReason> failureReasons;
}
