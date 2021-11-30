package io.castled.commons.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PipelineSyncStats {

    private long recordsSynced;
    private long recordsFailed;
    private long recordsSkipped;
    private long offset;

    public PipelineSyncStats(long recordsSynced, long recordsFailed, long recordsSkipped, long offset) {
        this.recordsSynced = recordsSynced;
        this.recordsFailed = recordsFailed;
        this.recordsSkipped = recordsSkipped;
        this.offset = offset;
    }


}
