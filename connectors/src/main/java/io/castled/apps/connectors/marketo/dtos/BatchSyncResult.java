package io.castled.apps.connectors.marketo.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchSyncResult {

    private Integer seq;
    private String status;
    private Integer id;
    private String marketoGUID;
    private List<ErrorResponse> reasons;

    public static final String UPDATED = "updated";
    public static final String CREATED = "created";
    public static final String SKIPPED = "skipped";
    public static final String FAILED = "failed";
}
