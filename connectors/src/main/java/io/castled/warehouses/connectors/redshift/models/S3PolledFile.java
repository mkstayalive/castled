package io.castled.warehouses.connectors.redshift.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class S3PolledFile {

    private String bucket;
    private String objectKey;
    private long contentLength;
}
