package io.castled.dtos;

import io.castled.apps.models.SyncObject;
import lombok.Data;

@Data
public class SubObjectsFetchRequest {
    private SyncObject syncObject;
}
