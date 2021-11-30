package io.castled.apps.connectors.activecampaign.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class BulkImportStatusInfoResponse {

    private String status;
    private List<String> success;
    private List<String> failure;
}
