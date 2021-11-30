package io.castled.apps.connectors.intercom;

import io.castled.apps.models.PrimaryKeyIdMapper;
import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.commons.models.AppSyncStats;
import io.castled.commons.models.MessageSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.schema.models.Message;
import io.castled.schema.models.Tuple;

import java.util.List;
import java.util.concurrent.TimeoutException;


public interface IntercomObjectSink<IDTYPE> {

    PrimaryKeyIdMapper<IDTYPE> getPrimaryKeyIdMapper();

    IntercomObjectSink<IDTYPE> initialize(IntercomObject intercomObject, AppSyncConfig appSyncConfig,
                                          IntercomAppConfig intercomAppConfig, ErrorOutputStream errorOutputStream,
                                          List<String> primaryKeys);

    void createObject(Message message);

    void updateObject(IDTYPE id, Message message);

    void flushRecords() throws TimeoutException;

    MessageSyncStats getSyncStats();

}
