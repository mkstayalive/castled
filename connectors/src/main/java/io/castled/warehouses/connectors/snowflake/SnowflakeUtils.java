package io.castled.warehouses.connectors.snowflake;

import io.castled.filestorage.CastledS3Client;
import io.castled.warehouses.WarehouseConfig;

public class SnowflakeUtils {

    public static CastledS3Client getS3Client(WarehouseConfig warehouseConfig, String encryptionKey) {

        SnowflakeWarehouseConfig snowflakeWarehouseConfig = (SnowflakeWarehouseConfig) warehouseConfig;
        return new CastledS3Client(snowflakeWarehouseConfig.getAccessKeyId(),
                snowflakeWarehouseConfig.getAccessKeySecret(), encryptionKey,
                snowflakeWarehouseConfig.getRegion(), snowflakeWarehouseConfig.getS3Bucket());
    }
}
