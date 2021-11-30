package io.castled.apps.connectors.salesforce.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PkChunking {
    private boolean enabled;
    private int chunkSize;
}
