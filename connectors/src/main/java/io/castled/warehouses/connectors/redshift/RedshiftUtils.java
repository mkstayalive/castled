package io.castled.warehouses.connectors.redshift;

import io.castled.filestorage.CastledS3Client;
import io.castled.warehouses.WarehouseConfig;

public class RedshiftUtils {

    public static CastledS3Client getS3Client(WarehouseConfig warehouseConfig, String encryptionKey) {

        RedshiftWarehouseConfig redshiftWarehouseConfig = (RedshiftWarehouseConfig) warehouseConfig;
        return new CastledS3Client(redshiftWarehouseConfig.getAccessKeyId(),
                redshiftWarehouseConfig.getAccessKeySecret(), encryptionKey,
                redshiftWarehouseConfig.getRegion(), redshiftWarehouseConfig.getS3Bucket());
    }
}
