package io.castled.warehouses;

import com.google.common.collect.Lists;
import io.castled.commons.streams.RecordOutputStream;
import io.castled.constants.ConnectorExecutionConstants;
import io.castled.schema.models.Field;
import io.castled.schema.models.FieldSchema;
import io.castled.schema.models.Tuple;
import io.castled.warehouses.models.WarehousePollContext;
import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.util.List;

public abstract class WarehouseSyncFailureListener implements RecordOutputStream {
    protected final List<String> trackableFields;
    protected final WarehousePollContext warehousePollContext;
    protected final Path failureRecordsDirectory;
    private boolean flushed = false;

    public WarehouseSyncFailureListener(WarehousePollContext warehousePollContext) {
        this.trackableFields = getTrackableFields(warehousePollContext);
        this.warehousePollContext = warehousePollContext;
        this.failureRecordsDirectory = ConnectorExecutionConstants.FAILURE_RECORDS_DIR.resolve(warehousePollContext.getPipelineUUID())
                .resolve(String.valueOf(warehousePollContext.getPipelineRunId()));
    }

    public void writeRecord(Tuple record) throws Exception {
        Tuple.Builder recordBuilder = Tuple.builder().name(record.getName());
        for (Field field : record.getFields()) {
            if (trackableFields.contains(field.getName())) {
                recordBuilder.put(field);
            }
        }
        doWriteRecord(recordBuilder.build());
    }

    private List<String> getTrackableFields(WarehousePollContext warehousePollContext) {
        List<String> trackableFields = Lists.newArrayList();
        for (FieldSchema fieldSchema : warehousePollContext.getWarehouseSchema().getFieldSchemas()) {
            if (CollectionUtils.isNotEmpty(warehousePollContext.getPrimaryKeys()) &&
                    !warehousePollContext.getPrimaryKeys().contains(fieldSchema.getName())) {
                continue;
            }
            trackableFields.add(fieldSchema.getName());
        }
        return trackableFields;
    }

    public abstract void doWriteRecord(Tuple record) throws Exception;

    public void flush() throws Exception {
        if (flushed) {
            return;
        }
        doFlush();
        flushed = true;
    }

    public abstract void cleanupResources(String pipelineUUID, Long pipelineRunId, WarehouseConfig warehouseConfig);

    public abstract void doFlush() throws Exception;
}
