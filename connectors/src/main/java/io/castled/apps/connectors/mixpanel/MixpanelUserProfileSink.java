package io.castled.apps.connectors.mixpanel;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.mixpanel.dto.UserProfileAndError;
import io.castled.apps.models.DataSinkRequest;
import io.castled.apps.models.GenericSyncObject;
import io.castled.commons.models.AppSyncStats;
import io.castled.commons.models.MessageSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.schema.models.Message;
import io.castled.schema.models.Tuple;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;


@Singleton
public class MixpanelUserProfileSink extends MixpanelObjectSink<Message> {

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

    public MixpanelUserProfileSink(DataSinkRequest dataSinkRequest) {
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

        List<UserProfileAndError> failedRecords = this.mixpanelRestClient.upsertUserProfileDetails(
                messages.stream().map(Message::getRecord).map(this::constructUserProfileDetails).collect(Collectors.toList()));

        Map<String, Message> userProfileRecordMapper = messages.stream().filter(message -> getDistinctID(message.getRecord()) != null)
                .collect(Collectors.toMap(message -> getDistinctID(message.getRecord()), Function.identity()));

        failedRecords.forEach(failedRecord ->
                failedRecord.getFailureReasons().forEach(failureReason -> this.errorOutputStream.writeFailedRecord(userProfileRecordMapper.get(failedRecord.getDistinctID()),
                        mixpanelErrorParser.getPipelineError(failureReason))));

        this.processedRecords.addAndGet(messages.size());
        this.lastProcessedOffset = Math.max(lastProcessedOffset, Iterables.getLast(messages).getOffset());
    }

    private String getDistinctID(Tuple record) {
        return (String) record.getValue(MixpanelObjectFields.USER_PROFILE_FIELDS.DISTINCT_ID.getFieldName());
    }

    private Map<String,Object> constructUserProfileDetails(Tuple record) {

        String firstName = (String) record.getValue(MixpanelObjectFields.USER_PROFILE_FIELDS.FIRST_NAME.getFieldName());
        String lastName = (String) record.getValue(MixpanelObjectFields.USER_PROFILE_FIELDS.LAST_NAME.getFieldName());
        String email = (String) record.getValue(MixpanelObjectFields.USER_PROFILE_FIELDS.EMAIL.getFieldName());
        String distinctID = (String) record.getValue(MixpanelObjectFields.USER_PROFILE_FIELDS.DISTINCT_ID.getFieldName());

        Map<String,Object> userProfileInfo = Maps.newHashMap();
        userProfileInfo.put("$token",mixpanelAppConfig.getProjectToken());
        userProfileInfo.put("$distinct_id",distinctID);
        userProfileInfo.put("$set",Maps.newHashMap());

        Map<String,Object> reservedPropertyMap = record.getFields().stream().
                filter(field -> isMixpanelReservedKeyword(field.getName())).collect(Collectors.toMap(field -> "$"+field.getName() ,field-> field.getValue()));

        Map<String,Object> nonReservedPropertyMap = record.getFields().stream().
                filter(field -> !isMixpanelReservedKeyword(field.getName())).collect(Collectors.toMap(field -> field.getName() ,field-> field.getValue()));

        if(!reservedPropertyMap.isEmpty()) {
            ((Map<String,Object>)userProfileInfo.get("$set")).putAll(reservedPropertyMap);
        }
        if(!nonReservedPropertyMap.isEmpty()) {
            ((Map<String,Object>)userProfileInfo.get("$set")).putAll(nonReservedPropertyMap);
        }

        return userProfileInfo;
    }

    private boolean isMixpanelReservedKeyword(String fieldName)
    {
        return getReservedKeywords().contains(fieldName);
    }

    private List<String> getReservedKeywords()
    {
        return Lists.newArrayList("region","timezone","country_code","last_seen","city","first_name","last_name","email");
    }

    @Override
    public MessageSyncStats getSyncStats() {
        return new MessageSyncStats(processedRecords.get(), lastProcessedOffset);
    }

    @Override
    public long getMaxBufferedObjects() {
        return 200;
    }
}
