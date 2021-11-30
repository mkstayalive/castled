package io.castled.utils;

import com.google.common.collect.Lists;
import io.castled.constants.ConnectorExecutionConstants;
import io.castled.models.CastledDataMapping;
import io.castled.models.Pipeline;

import java.nio.file.Path;
import java.util.List;

public class PipelineUtils {

    public static List<String> getWarehousePrimaryKeys(Pipeline pipeline) {
        return getWarehouseFields(pipeline.getDataMapping(), pipeline.getDataMapping().getPrimaryKeys());
    }

    public static List<String> getWarehouseFields(CastledDataMapping dataMapping, List<String> appFields) {
        return Lists.newArrayList(dataMapping.getMappingForAppFields(appFields).values());
    }

    public static Path getAppUploadPath(String pipelineId, Long pipelineRunId) {
        return ConnectorExecutionConstants.APP_UPLOADS_PATH.resolve(pipelineId).resolve(String.valueOf(pipelineRunId));
    }
}
