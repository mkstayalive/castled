package io.castled.warehouses.connectors.redshift.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedshiftS3CopyManifest {

    private List<ManifestEntry> entries;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ManifestEntry {
        private String url;
        private boolean mandatory;
    }
}
