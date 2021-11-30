package io.castled.commons.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppSyncStats extends MessageSyncStats {

    public AppSyncStats(long recordsProcessed, long offset, long recordsSkipped) {
        super(recordsProcessed, offset);
        this.recordsSkipped = recordsSkipped;
    }

    private long recordsSkipped;
}
