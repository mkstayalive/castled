package io.castled.apps.connectors.mixpanel;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.apps.ExternalAppConnector;
import io.castled.apps.ExternalAppType;
import io.castled.apps.dtos.AppSyncConfigDTO;
import io.castled.apps.models.ExternalAppSchema;
import io.castled.apps.models.GenericSyncObject;
import io.castled.apps.models.PrimaryKeyEligibles;
import io.castled.commons.models.AppSyncMode;
import io.castled.dtos.PipelineConfigDTO;
import io.castled.forms.dtos.FormFieldOption;
import io.castled.models.FieldMapping;
import io.castled.schema.models.FieldSchema;
import io.castled.schema.models.RecordSchema;
import org.apache.commons.collections.CollectionUtils;

import javax.ws.rs.BadRequestException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class MixpanelAppConnector implements ExternalAppConnector<MixpanelAppConfig,
        MixpanelDataSink, MixpanelAppSyncConfig> {

    @Override
    public List<FormFieldOption> getAllObjects(MixpanelAppConfig config, MixpanelAppSyncConfig customerIOAppSyncConfig) {
        return Arrays.stream(MixpanelObject.values()).map(mixpanelObject -> new FormFieldOption(new GenericSyncObject(mixpanelObject.getName(),
                ExternalAppType.MIXPANEL), mixpanelObject.getName())).collect(Collectors.toList());
    }

    @Override
    public MixpanelDataSink getDataSink() {
        return ObjectRegistry.getInstance(MixpanelDataSink.class);
    }

    @Override
    public ExternalAppSchema getSchema(MixpanelAppConfig config, MixpanelAppSyncConfig mixpanelAppSyncConfig) {
        return new ExternalAppSchema(null, PrimaryKeyEligibles.autoDetect());
    }

    public List<AppSyncMode> getSyncModes(MixpanelAppConfig config, MixpanelAppSyncConfig mixpanelAppSyncConfig) {
        String object = mixpanelAppSyncConfig.getObject().getObjectName();
        if(MixpanelObject.EVENT.getName().equalsIgnoreCase(object)) {
            return Lists.newArrayList(AppSyncMode.INSERT);
        }
        if(MixpanelObject.USER_PROFILE.getName().equalsIgnoreCase(object) ||
                MixpanelObject.GROUP_PROFILE.getName().equalsIgnoreCase(object)) {
            return Lists.newArrayList(AppSyncMode.UPSERT);
        }
        return Lists.newArrayList(AppSyncMode.INSERT,AppSyncMode.UPSERT,AppSyncMode.UPDATE);
    }

    public Class<MixpanelAppSyncConfig> getMappingConfigType() {
        return MixpanelAppSyncConfig.class;
    }

    @Override
    public Class<MixpanelAppConfig> getAppConfigType() {
        return MixpanelAppConfig.class;
    }

    public PipelineConfigDTO validateAndEnrichPipelineConfig(PipelineConfigDTO pipelineConfig) throws BadRequestException {
        MixpanelAppSyncConfig customerIOAppSyncConfig = (MixpanelAppSyncConfig) pipelineConfig.getAppSyncConfig();
        String objectName = pipelineConfig.getAppSyncConfig().getObject().getObjectName();

        if(MixpanelObject.EVENT.getName().equalsIgnoreCase(objectName)) {
            enrichPipelineConfigForEventObject(pipelineConfig, customerIOAppSyncConfig);
        }
        if(MixpanelObject.USER_PROFILE.getName().equalsIgnoreCase(objectName)){
            enrichPipelineConfigForUserProfileObject(pipelineConfig, customerIOAppSyncConfig);
        }
        if(MixpanelObject.GROUP_PROFILE.getName().equalsIgnoreCase(objectName)){
            enrichPipelineConfigForGroupProfileObject(pipelineConfig, customerIOAppSyncConfig);
        }
        return pipelineConfig;
    }

    private void enrichPipelineConfigForUserProfileObject(PipelineConfigDTO pipelineConfig, MixpanelAppSyncConfig customerIOAppSyncConfig) throws BadRequestException{

        String distinctID = Optional.ofNullable(customerIOAppSyncConfig.getDistinctID()).orElseThrow(()->new BadRequestException("Column uniquely identifying the User is mandatory"));

        List<FieldMapping> additionalMapping = Lists.newArrayList();
        Optional.ofNullable(distinctID).ifPresent((ID) -> additionalMapping.add(new FieldMapping(ID,MixpanelObjectFields.USER_PROFILE_FIELDS.DISTINCT_ID.getFieldName(),false)));
        Optional.ofNullable(customerIOAppSyncConfig.getLastName()).ifPresent(lastName -> additionalMapping.add(new FieldMapping(lastName,MixpanelObjectFields.USER_PROFILE_FIELDS.LAST_NAME.getFieldName(),false)));
        Optional.ofNullable(customerIOAppSyncConfig.getFirstName()).ifPresent((firstName) -> additionalMapping.add(new FieldMapping(firstName,MixpanelObjectFields.USER_PROFILE_FIELDS.FIRST_NAME.getFieldName(),false)));
        Optional.ofNullable(customerIOAppSyncConfig.getUserEmail()).ifPresent((email) -> additionalMapping.add(new FieldMapping(email,MixpanelObjectFields.USER_PROFILE_FIELDS.EMAIL.getFieldName(),false)));
        pipelineConfig.getMapping().addAdditionalMappings(additionalMapping);

        pipelineConfig.getMapping().setPrimaryKeys(Collections.singletonList(MixpanelObjectFields.USER_PROFILE_FIELDS.DISTINCT_ID.getFieldName()));
    }

    private void enrichPipelineConfigForGroupProfileObject(PipelineConfigDTO pipelineConfig, MixpanelAppSyncConfig customerIOAppSyncConfig) throws BadRequestException{
        String groupID = Optional.ofNullable(customerIOAppSyncConfig.getGroupID()).orElseThrow(()->new BadRequestException("Column uniquely identifying the Group is mandatory"));
        String groupKey = Optional.ofNullable(customerIOAppSyncConfig.getGroupKey()).orElseThrow(()->new BadRequestException("Group key is mandatory"));

        List<FieldMapping> additionalMapping = Lists.newArrayList();
        Optional.ofNullable(groupID).ifPresent((ID) -> additionalMapping.add(new FieldMapping(ID,MixpanelObjectFields.GROUP_PROFILE_FIELDS.GROUP_ID.getFieldName(),false)));
        pipelineConfig.getMapping().addAdditionalMappings(additionalMapping);

        pipelineConfig.getMapping().setPrimaryKeys(Collections.singletonList(MixpanelObjectFields.GROUP_PROFILE_FIELDS.GROUP_ID.getFieldName()));
    }

    private void enrichPipelineConfigForEventObject(PipelineConfigDTO pipelineConfig, MixpanelAppSyncConfig customerIOAppSyncConfig) throws BadRequestException{

        String eventId = Optional.ofNullable(customerIOAppSyncConfig.getEventID()).orElseThrow(()->new BadRequestException("Column uniquely identifying the Event is mandatory"));
        String distinctId = Optional.ofNullable(customerIOAppSyncConfig.getDistinctIDForEvent()).orElseThrow(()->new BadRequestException("Column uniquely identifying the User is mandatory"));

        List<FieldMapping> additionalMapping = Lists.newArrayList();
        Optional.ofNullable(eventId).ifPresent((insertID) -> additionalMapping.add(new FieldMapping(insertID,MixpanelObjectFields.EVENT_FIELDS.INSERT_ID.getFieldName(),false)));
        Optional.ofNullable(distinctId).ifPresent((distinctID) -> additionalMapping.add(new FieldMapping(distinctID,MixpanelObjectFields.EVENT_FIELDS.DISTINCT_ID.getFieldName(),false)));
        Optional.ofNullable(customerIOAppSyncConfig.getEventName()).ifPresent((eventName) -> additionalMapping.add(new FieldMapping(eventName,MixpanelObjectFields.EVENT_FIELDS.EVENT_NAME.getFieldName(),false)));
        Optional.ofNullable(customerIOAppSyncConfig.getEventIP()).ifPresent((eventIP) -> additionalMapping.add(new FieldMapping(eventIP,MixpanelObjectFields.EVENT_FIELDS.GEO_IP.getFieldName(),false)));
        Optional.ofNullable(customerIOAppSyncConfig.getEventTimeStamp()).ifPresent((eventTimeStamp) -> additionalMapping.add(new FieldMapping(eventTimeStamp,MixpanelObjectFields.EVENT_FIELDS.EVENT_TIMESTAMP.getFieldName(),false)));
        pipelineConfig.getMapping().addAdditionalMappings(additionalMapping);

        pipelineConfig.getMapping().setPrimaryKeys(Collections.singletonList(MixpanelObjectFields.EVENT_FIELDS.INSERT_ID.getFieldName()));
    }

    @Override
    public RecordSchema enrichWarehouseASchema(AppSyncConfigDTO appSyncConfigDTO , RecordSchema warehouseSchema) {

        MixpanelAppSyncConfig mixpanelAppSyncConfig = ((MixpanelAppSyncConfig)appSyncConfigDTO.getAppSyncConfig());
        String objectName = mixpanelAppSyncConfig.getObject().getObjectName();

        if(MixpanelObject.EVENT.getName().equalsIgnoreCase(objectName)) {
            List<String> warehouseFieldsToBeRemoved = getAllReservedFieldsForEventProfile(mixpanelAppSyncConfig);
            List<FieldSchema> fieldSchemas = warehouseSchema.getFieldSchemas().stream().filter(schema -> warehouseFieldsToBeRemoved.contains(schema.getName())).collect(Collectors.toList());
            warehouseSchema.removeFieldSchema(fieldSchemas);
        }
        if(MixpanelObject.USER_PROFILE.getName().equalsIgnoreCase(objectName)){
            List<String> warehouseFieldsToBeRemoved = getAllReservedFieldsForUserProfile(mixpanelAppSyncConfig);
            List<FieldSchema> fieldSchemas = warehouseSchema.getFieldSchemas().stream().filter(schema -> warehouseFieldsToBeRemoved.contains(schema.getName())).collect(Collectors.toList());
            warehouseSchema.removeFieldSchema(fieldSchemas);
        }
        if(MixpanelObject.GROUP_PROFILE.getName().equalsIgnoreCase(objectName)){
            List<String> warehouseFieldsToBeRemoved = getAllReservedFieldsForGroupProfile(mixpanelAppSyncConfig);
            List<FieldSchema> fieldSchemas = warehouseSchema.getFieldSchemas().stream().filter(schema -> warehouseFieldsToBeRemoved.contains(schema.getName())).collect(Collectors.toList());
            warehouseSchema.removeFieldSchema(fieldSchemas);
        }

        return warehouseSchema;
    }

    private List<String> getAllReservedFieldsForUserProfile(MixpanelAppSyncConfig mixpanelAppSyncConfig ){
        List<String> reservedFields = Lists.newArrayList();
        CollectionUtils.addIgnoreNull(reservedFields,mixpanelAppSyncConfig.getDistinctID());
        CollectionUtils.addIgnoreNull(reservedFields,mixpanelAppSyncConfig.getFirstName());
        CollectionUtils.addIgnoreNull(reservedFields,mixpanelAppSyncConfig.getLastName());
        CollectionUtils.addIgnoreNull(reservedFields,mixpanelAppSyncConfig.getUserEmail());
        return reservedFields;
    }

    private List<String> getAllReservedFieldsForGroupProfile(MixpanelAppSyncConfig mixpanelAppSyncConfig ){
        List<String> reservedFields = Lists.newArrayList();
        CollectionUtils.addIgnoreNull(reservedFields,mixpanelAppSyncConfig.getGroupID());
        CollectionUtils.addIgnoreNull(reservedFields,mixpanelAppSyncConfig.getGroupKey());
        return reservedFields;
    }

    private List<String> getAllReservedFieldsForEventProfile(MixpanelAppSyncConfig mixpanelAppSyncConfig ){
        List<String> reservedFields = Lists.newArrayList();
        CollectionUtils.addIgnoreNull(reservedFields,mixpanelAppSyncConfig.getEventID());
        CollectionUtils.addIgnoreNull(reservedFields,mixpanelAppSyncConfig.getEventIP());
        CollectionUtils.addIgnoreNull(reservedFields,mixpanelAppSyncConfig.getEventName());
        CollectionUtils.addIgnoreNull(reservedFields,mixpanelAppSyncConfig.getEventTimeStamp());
        CollectionUtils.addIgnoreNull(reservedFields,mixpanelAppSyncConfig.getDistinctIDForEvent());
        return reservedFields;
    }
}
