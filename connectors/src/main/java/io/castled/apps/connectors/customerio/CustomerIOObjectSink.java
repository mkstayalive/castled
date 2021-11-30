package io.castled.apps.connectors.customerio;

import io.castled.apps.connectors.intercom.IntercomAppConfig;
import io.castled.apps.connectors.intercom.IntercomObject;
import io.castled.apps.models.PrimaryKeyIdMapper;
import io.castled.apps.syncconfigs.AppSyncConfig;
import io.castled.commons.models.MessageSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.schema.models.Message;

import java.util.List;
import java.util.concurrent.TimeoutException;


public interface CustomerIOObjectSink<IDTYPE> {

    void createOrUpdateObject(Message message);

    void flushRecords() throws TimeoutException;

    MessageSyncStats getSyncStats();

}
