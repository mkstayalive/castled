package io.castled.warehouses;

import lombok.Data;

@Data
public class WarehouseConnectorConfig {

    //in mbs;
    private int unloadFileSize = 8;

    //in GBs;
    private int failedRecordBufferSize = 20;

    private int failedMaxRecordsCount = 50000;
}
