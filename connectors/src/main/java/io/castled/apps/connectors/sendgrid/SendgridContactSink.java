package io.castled.apps.connectors.sendgrid;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import io.castled.ObjectRegistry;
import io.castled.apps.BufferedObjectSink;
import io.castled.apps.connectors.sendgrid.dtos.ContactAttribute;
import io.castled.commons.models.AppSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.schema.models.Message;
import io.castled.schema.models.Tuple;
import org.apache.commons.io.FileUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SendgridContactSink extends BufferedObjectSink<Message>  {

    // Sendgrid API Limits
    private static final long UPSERT_BATCH_NUM_SIZE_MAX = 30000;
    private static final long UPSERT_BATCH_BYTES_SIZE_MAX = 6 * FileUtils.ONE_MB;
    // If we assume each field in the request is consuming max allowed size,
    // with 6K records we hit 6MB request size limitation.
    // But for now going with batch size of 30K itself due to large overhead of a batch upsert api call.
    private static final long UPSERT_BATCH_NUM_SIZE_MAX_CHOSEN = UPSERT_BATCH_NUM_SIZE_MAX;

    private final SendgridRestClient sendgridRestClient;
    private final ErrorOutputStream errorOutputStream;
    private final SendgridAppSyncConfig syncConfig;
    private final SendgridErrorParser errorParser;
    private final AppSyncStats syncStats;

    public SendgridContactSink(SendgridAppConfig sendgridAppConfig,
                               SendgridAppSyncConfig syncConfig,
                               ErrorOutputStream errorOutputStream) {
        this.sendgridRestClient = new SendgridRestClient(sendgridAppConfig);
        this.errorOutputStream = errorOutputStream;
        this.errorParser = ObjectRegistry.getInstance(SendgridErrorParser.class);
        this.syncConfig = syncConfig;
        this.syncStats = new AppSyncStats(0,0,0);
    }

    @Override
    protected void writeRecords(List<Message> msgs) {
        List<Map<String, Object>> contacts = msgs.stream().map(msgRef ->
                constructContactProperties(msgRef.getRecord())).collect(Collectors.toList());

        List<SendgridUpsertError> upsertErrors = sendgridRestClient.upsertContacts(contacts, syncConfig.getListIds());

        Map<String, Message> emailRecordMap = msgs.stream()
                .collect(Collectors.toMap(message -> getEmail(message.getRecord()), Function.identity()));
        upsertErrors.forEach(error -> this.errorOutputStream.writeFailedRecord(emailRecordMap.get(error.getEmail()),
                errorParser.getPipelineError(error)));
        updateStats(msgs.size(), Iterables.getLast(msgs).getOffset());
    }

    @Override
    public long getMaxBufferedObjects() {
        return UPSERT_BATCH_NUM_SIZE_MAX_CHOSEN;
    }

    private String getEmail(Tuple record) {
        return (String) record.getValue(ContactAttribute.EMAIL);
    }

   private Map<String, Object> constructContactProperties(Tuple record) {
        final String CUSTOM_TAG = "custom";
        Map<String, Object> reservedProperties = Maps.newHashMap();
        record.getFields().stream().filter(fieldRef -> !(boolean)fieldRef.getParams().get(CUSTOM_TAG))
                .forEach(fieldRef -> reservedProperties.put(fieldRef.getName(),
                        SendgridRequestFormatterUtils.formatValue(fieldRef.getValue(), fieldRef.getSchema())));

        Map<String, Object> customProperties = Maps.newHashMap();
        record.getFields().stream().filter(fieldRef -> (boolean)fieldRef.getParams().get(CUSTOM_TAG))
                .forEach(fieldRef -> customProperties.put(fieldRef.getName(),
                        SendgridRequestFormatterUtils.formatValue(fieldRef.getValue(), fieldRef.getSchema())));

        reservedProperties.put(ContactAttribute.CUSTOM_FIELDS, customProperties);
        return reservedProperties;
    }

    private void updateStats(long processed, long maxOffset) {
        syncStats.setRecordsProcessed(syncStats.getRecordsProcessed() + processed);
        syncStats.setOffset(Math.max(syncStats.getOffset(), maxOffset));
    }

    public AppSyncStats getSyncStats() {
        return syncStats;
    }
}
