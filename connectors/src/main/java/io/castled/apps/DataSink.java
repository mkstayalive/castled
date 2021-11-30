package io.castled.apps;

import io.castled.apps.models.DataSinkRequest;
import io.castled.commons.models.AppSyncStats;

public interface DataSink {

    void syncRecords(DataSinkRequest dataSinkRequest) throws Exception;

    AppSyncStats getSyncStats();
}
