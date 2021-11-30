package io.castled.warehouses.connectors.bigquery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BQSnapshotTracker {

    private Long id;
    private String pipelineUUID;
    private String committedSnapshot;
    private String uncommittedSnapshot;
}
