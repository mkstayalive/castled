package io.castled.apps.connectors.mixpanel;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.activecampaign.constant.ActiveCampaignConstants;
import io.castled.apps.connectors.mixpanel.dto.GroupProfileAndError;
import io.castled.apps.models.DataSinkRequest;
import io.castled.apps.models.GenericSyncObject;
import io.castled.commons.models.AppSyncStats;
import io.castled.commons.models.MessageSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.schema.models.Field;
import io.castled.schema.models.Message;
import io.castled.schema.models.Tuple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;


@Singleton
public class MixpanleGroupProfileSink extends MixpanelObjectSink<Message> {

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

    public MixpanleGroupProfileSink(DataSinkRequest dataSinkRequest) {
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

        List<GroupProfileAndError> failedRecords = this.mixpanelRestClient.upsertGroupProfileDetails(
                messages.stream().map(Message::getRecord).map(this::constructGroupProfileDetails).collect(Collectors.toList()));

        Map<String, Message> groupProfileRecordMapper = messages.stream().filter(message -> getGroupID(message.getRecord()) != null)
                .collect(Collectors.toMap(message -> getGroupID(message.getRecord()), Function.identity()));

        failedRecords.forEach(failedRecord ->
                failedRecord.getFailureReasons().forEach(failureReason -> this.errorOutputStream.writeFailedRecord(groupProfileRecordMapper.get(failedRecord.getGroupID()),
                        mixpanelErrorParser.getPipelineError(failureReason))));

        this.processedRecords.addAndGet(messages.size());
        this.lastProcessedOffset = Math.max(lastProcessedOffset, Iterables.getLast(messages).getOffset());
    }

    private String getGroupID(Tuple record) {
        return (String) record.getValue(MixpanelObjectFields.GROUP_PROFILE_FIELDS.GROUP_ID.getFieldName());
    }

    private Map<String,Object> constructGroupProfileDetails(Tuple record) {

        String groupID = (String) record.getValue(MixpanelObjectFields.GROUP_PROFILE_FIELDS.GROUP_ID.getFieldName());

        Map<String,Object> groupProfileInfo = Maps.newHashMap();
        groupProfileInfo.put("$group_key",syncConfig.getGroupKey());
        groupProfileInfo.put("$group_id",groupID);
        groupProfileInfo.put("$set",Maps.newHashMap());
        Map<String,Object> nonReservedPropertyMap = record.getFields().stream().
                filter(field -> !isMixpanelReservedKeyword(field.getName())).collect(Collectors.toMap(field -> field.getName() ,field-> field.getValue()));
        if(!nonReservedPropertyMap.isEmpty()) {
            ((Map<String,Object>)groupProfileInfo.get("$set")).putAll(nonReservedPropertyMap);
        }

        return groupProfileInfo;
    }

    private Integer transformFieldId(Field field)
    {
        return (Integer) Optional.ofNullable(field.getParams().get(ActiveCampaignConstants.CUSTOM_FIELD_ID))
                .filter(objectRef -> objectRef instanceof Integer).orElse(null);
    }

    private String transformFieldValue(Field field)
    {
        Object object = field.getValue();
        if(object instanceof Integer) {
            return String.valueOf(object);
        }
        else if(object instanceof String) {
            return (String) object;
        }
        else if (object instanceof LocalDate) {
            return ((LocalDate) object).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        else if (object instanceof LocalDateTime) {
            return ((LocalDateTime) object).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
        }
        return null;
    }

    private boolean isMixpanelReservedKeyword(String fieldName)
    {
        return getReservedKeywords().contains(fieldName);
    }

    private List<String> getReservedKeywords()
    {
        return Lists.newArrayList("group_id","group_key");
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
