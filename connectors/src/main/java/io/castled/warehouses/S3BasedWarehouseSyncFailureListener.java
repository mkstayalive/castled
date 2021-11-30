package io.castled.warehouses;

import com.google.common.collect.Lists;
import io.castled.commons.models.FileStorageNamespace;
import io.castled.filestorage.CastledS3Client;
import io.castled.utils.FileUtils;
import io.castled.warehouses.models.WarehousePollContext;

public abstract class S3BasedWarehouseSyncFailureListener extends WarehouseSyncFailureListener {

    protected final CastledS3Client castledS3Client;

    public S3BasedWarehouseSyncFailureListener(WarehousePollContext warehousePollContext,
                                               CastledS3Client castledS3Client) {
        super(warehousePollContext);
        this.castledS3Client = castledS3Client;
    }

    @Override
    public void cleanupResources(String pipelineUUID, Long pipelineRunId, WarehouseConfig warehouseConfig) {
        castledS3Client.deleteDirectory(getS3FailedRecordsDirectory(pipelineUUID, pipelineRunId));
        FileUtils.deleteDirectory(failureRecordsDirectory);
    }


    public String getS3FailedRecordsDirectory(String pipelineUUID, Long pipelineRunId) {
        return CastledS3Client.constructObjectKey
                (Lists.newArrayList(FileStorageNamespace.PIPELINE_FAILED_RECORDS.getNamespace(), pipelineUUID, String.valueOf(pipelineRunId)));
    }
}
