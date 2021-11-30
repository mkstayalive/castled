package io.castled.warehouses;

import io.castled.exceptions.connect.ConnectException;
import io.castled.forms.dtos.FormFieldsDTO;
import io.castled.models.QueryResults;
import io.castled.schema.models.RecordSchema;
import io.castled.warehouses.models.WarehousePollContext;

import java.util.List;

public interface WarehouseConnector<CONFIG extends WarehouseConfig> {

    void testConnectionForDataPoll(CONFIG config) throws ConnectException;

    WarehouseDataPoller getDataPoller();

    RecordSchema getQuerySchema(CONFIG config, String query) throws Exception;

    WarehouseSyncFailureListener syncFailureListener(WarehousePollContext warehousePollContext) throws Exception;

    default TableProperties getSnapshotTableProperties(List<String> recordIdKeys) {
        return null;
    }

    void restartPoll(String pipelineUUID, CONFIG config) throws Exception;

    QueryResults previewQuery(String query, CONFIG config, int maxRows) throws Exception;

    FormFieldsDTO getFormFields();

}
