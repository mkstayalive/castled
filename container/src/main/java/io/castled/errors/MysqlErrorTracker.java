package io.castled.errors;

import com.google.common.collect.Maps;
import io.castled.ObjectRegistry;
import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.CastledErrorTracker;
import io.castled.daos.ErrorReportsDAO;
import io.castled.models.ErrorReport;
import io.castled.schema.SchemaUtils;
import io.castled.schema.models.Field;
import io.castled.schema.models.Tuple;
import io.castled.utils.JsonUtils;
import io.castled.warehouses.models.WarehousePollContext;
import org.apache.commons.collections4.CollectionUtils;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Map;

public class MysqlErrorTracker implements CastledErrorTracker {

    private boolean flushed = false;

    private final WarehousePollContext warehousePollContext;
    private List<String> schemaFields;
    private final Map<String, PipelineErrorAndSample> errorAndSamples = Maps.newHashMap();
    private final ErrorReportsDAO errorReportsDAO;

    public MysqlErrorTracker(WarehousePollContext warehousePollContext) {
        this.warehousePollContext = warehousePollContext;
        this.errorReportsDAO = ObjectRegistry.getInstance(Jdbi.class).onDemand(ErrorReportsDAO.class);
    }

    public synchronized void writeError(Tuple record, CastledError pipelineError) {
        String uniqueKey = pipelineError.getErrorCode() + "_" + pipelineError.uniqueId();
        if (CollectionUtils.isEmpty(schemaFields)) {
            schemaFields = SchemaUtils.getFieldNames(record);
        }
        if (errorAndSamples.containsKey(uniqueKey)) {
            errorAndSamples.get(uniqueKey).incrementRecordCount();
        } else {
            errorAndSamples.put(uniqueKey, new PipelineErrorAndSample(pipelineError.getErrorCode(),
                    pipelineError.description(), transformStructToMap(record), 1));
        }
    }

    private Map<String, String> transformStructToMap(Tuple record) {
        Map<String, String> recordMap = Maps.newHashMap();
        for (Field field : record.getFields()) {
            Object recordValue = record.getValue(field.getName());
            if (recordValue != null) {
                recordMap.put(field.getName(), recordValue.toString());
            }
        }
        return recordMap;
    }

    public void flushErrors() throws Exception {
        if (flushed) {
            return;
        }
        if (!errorAndSamples.isEmpty()) {
            StringBuilder errorBuilder = new StringBuilder(JsonUtils.objectToString(schemaFields));
            errorBuilder.append(System.lineSeparator());
            for (String uniqueKey : errorAndSamples.keySet()) {
                errorBuilder.append(JsonUtils.objectToString(errorAndSamples.get(uniqueKey)));
                errorBuilder.append(System.lineSeparator());
            }
            errorReportsDAO.createErrorReport(ErrorReport.builder().pipelineId(warehousePollContext.getPipelineId())
                    .pipelineRunId(warehousePollContext.getPipelineRunId())
                    .report(errorBuilder.toString()).build());
        }
        flushed = true;

    }
}
