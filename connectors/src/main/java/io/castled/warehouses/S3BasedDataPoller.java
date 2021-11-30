package io.castled.warehouses;

import com.google.common.collect.Lists;
import io.castled.commons.models.FileStorageNamespace;
import io.castled.filestorage.CastledS3Client;
import io.castled.utils.FileUtils;
import io.castled.warehouses.models.WarehousePollContext;

public abstract class S3BasedDataPoller implements WarehouseDataPoller {


    public void cleanupPipelineRunResources(WarehousePollContext warehousePollContext) {
        CastledS3Client castledS3Client = getS3Client(warehousePollContext.getWarehouseConfig(),
                warehousePollContext.getDataEncryptionKey());
        castledS3Client.deleteDirectory(getS3UnloadDirectory(warehousePollContext.getPipelineUUID(),
                warehousePollContext.getPipelineRunId()));
        FileUtils.deleteDirectory(getPipelineRunUnloadDirectory(warehousePollContext.getPipelineUUID(),
                warehousePollContext.getPipelineRunId()));
    }


    public String getS3UnloadDirectory(String pipelineUUID, Long pipelineRunId) {
        return CastledS3Client.constructObjectKey
                (Lists.newArrayList(FileStorageNamespace.PIPELINE_UNLOADS.getNamespace(), pipelineUUID, String.valueOf(pipelineRunId)));
    }

    public abstract CastledS3Client getS3Client(WarehouseConfig warehouseConfig, String encryptionKey);

}
