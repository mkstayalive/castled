package io.castled.apps.connectors.mixpanel;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.mixpanel.dto.EventAndError;
import io.castled.apps.models.DataSinkRequest;
import io.castled.apps.models.GenericSyncObject;
import io.castled.commons.models.AppSyncStats;
import io.castled.commons.models.MessageSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.schema.models.Message;
import io.castled.schema.models.Tuple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;


@Singleton
public class MixpanelEventSink extends MixpanelObjectSink<Message> {

    private final MixpanelRestClient mixpanelRestClient;
    private final MixpanelErrorParser mixpanelErrorParser;
    private final ErrorOutputStream errorOutputStream;
    private final GenericSyncObject userProfileSyncObject;
    private final MixpanelAppConfig mixpanelAppConfig;
    private final AtomicLong processedRecords = new AtomicLong(0);
    private long lastProcessedOffset = 0;
    private final MixpanelAppSyncConfig syncConfig;
    private final AppSyncStats syncStats;
    private final List<String> primaryKeys;
    private final List<String> mappedFields;

    private final AtomicLong failedRecords = new AtomicLong(0);

    public MixpanelEventSink(DataSinkRequest dataSinkRequest) {
        this.mixpanelRestClient = new MixpanelRestClient(((MixpanelAppConfig) dataSinkRequest.getExternalApp().getConfig()).getProjectToken(),
                ((MixpanelAppConfig) dataSinkRequest.getExternalApp().getConfig()).getApiSecret());
        this.errorOutputStream = dataSinkRequest.getErrorOutputStream();
        this.mixpanelErrorParser = ObjectRegistry.getInstance(MixpanelErrorParser.class);
        this.userProfileSyncObject = (GenericSyncObject) dataSinkRequest.getAppSyncConfig().getObject();
        this.syncConfig = (MixpanelAppSyncConfig) dataSinkRequest.getAppSyncConfig();
        this.primaryKeys = dataSinkRequest.getPrimaryKeys();
        this.syncStats = new AppSyncStats(0, 0, 0);
        this.mappedFields = dataSinkRequest.getMappedFields();
        this.mixpanelAppConfig = (MixpanelAppConfig) dataSinkRequest.getExternalApp().getConfig();
    }


    @Override
    protected void writeRecords(List<Message> messages) {

        List<EventAndError> failedRecords = this.mixpanelRestClient.insertEventDetails(
                messages.stream().map(Message::getRecord).map(this::constructEventDetails).collect(Collectors.toList()));

        Map<String, Message> eventIDMapper = messages.stream().filter(message -> getEventID(message.getRecord()) != null)
                .collect(Collectors.toMap(message -> getEventID(message.getRecord()), Function.identity()));

        failedRecords.forEach(failedRecord ->
                failedRecord.getFailureReasons().forEach(failureReason -> this.errorOutputStream.writeFailedRecord(eventIDMapper.get(failedRecord.getInsertId()),
                        mixpanelErrorParser.getPipelineError(failureReason))));

        this.processedRecords.addAndGet(messages.size());
        this.lastProcessedOffset = Math.max(lastProcessedOffset, Iterables.getLast(messages).getOffset());
    }

    private String getEventID(Tuple record)
    {
        return (String) record.getValue(MixpanelObjectFields.EVENT_FIELDS.INSERT_ID.getFieldName());
    }

    private Map<String,Object> constructEventDetails(Tuple record) {

        String eventName = (String) record.getValue(MixpanelObjectFields.EVENT_FIELDS.EVENT_NAME.getFieldName());
        String insertId = (String) record.getValue(MixpanelObjectFields.EVENT_FIELDS.INSERT_ID.getFieldName());
        String distinctId = (String) record.getValue(MixpanelObjectFields.EVENT_FIELDS.DISTINCT_ID.getFieldName());
        Long timestamp = convertTimeStampToEpoch(record.getValue(MixpanelObjectFields.EVENT_FIELDS.EVENT_TIMESTAMP.getFieldName()));
        String geoIP = (String) record.getValue(MixpanelObjectFields.EVENT_FIELDS.GEO_IP.getFieldName());

        Map<String,Object> eventInfo = Maps.newHashMap();
        eventInfo.put("event",eventName);

        Map<String, Object> propertiesMap = Maps.newHashMap();
        propertiesMap.put(MixpanelObjectFields.EVENT_FIELDS.EVENT_NAME.getFieldName(),eventName);
        propertiesMap.put("$"+MixpanelObjectFields.EVENT_FIELDS.INSERT_ID.getFieldName(),insertId);
        propertiesMap.put(MixpanelObjectFields.EVENT_FIELDS.DISTINCT_ID.getFieldName(),distinctId);
        propertiesMap.put(MixpanelObjectFields.EVENT_FIELDS.EVENT_TIMESTAMP.getFieldName(),timestamp);
        propertiesMap.put(MixpanelObjectFields.EVENT_FIELDS.GEO_IP.getFieldName(),geoIP);
        //copy all non reserved properties from record
        propertiesMap.putAll(record.getFields().stream().
                filter(field -> !isMixpanelReservedKeyword(field.getName())).collect(Collectors.toMap(field -> field.getName() ,field-> transformFieldValue(field.getValue()))));
        eventInfo.put("properties",propertiesMap);

        return eventInfo;
    }

    private String transformFieldValue(Object object)
    {
        if(object instanceof Integer || object instanceof Long) {
            return String.valueOf(object);
        }
        else if(object instanceof String) {
            return (String) object;
        }
        else if (object instanceof LocalDate) {
            return ((LocalDate) object).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        else if (object instanceof LocalDateTime) {
            return ((LocalDate) object).format(DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:mm:ssZ"));
        }
        else if(object instanceof String)
        {
            return (String) object;
        }
        return null;
    }

    private Long convertTimeStampToEpoch(Object timestamp)
    {
        if(timestamp instanceof LocalDateTime)
        {
            return ((LocalDateTime) timestamp).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();  // 1055545912454
        }
        return null;
    }

    private boolean isMixpanelReservedKeyword(String fieldName)
    {
        return getReservedKeywords().contains(fieldName);
    }

    private List<String> getReservedKeywords()
    {
        return Lists.newArrayList("event","time","distinct_id","insert_id","ip");
    }

    @Override
    public MessageSyncStats getSyncStats() {
        return new MessageSyncStats(processedRecords.get(), lastProcessedOffset);
    }

    @Override
    public long getMaxBufferedObjects() {
        return 1000;
    }
}
