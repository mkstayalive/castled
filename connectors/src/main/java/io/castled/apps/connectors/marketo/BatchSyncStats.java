package io.castled.apps.connectors.marketo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BatchSyncStats {
    private long skipped;
    private List<MarketoSyncError> errors;
}
