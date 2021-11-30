package io.castled.apps.connectors.googleads;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v7.enums.ConversionActionTypeEnum;
import com.google.ads.googleads.v7.enums.CustomerMatchUploadKeyTypeEnum;
import com.google.ads.googleads.v7.resources.ConversionAction;
import com.google.ads.googleads.v7.resources.ConversionCustomVariable;
import com.google.ads.googleads.v7.services.GoogleAdsRow;
import com.google.ads.googleads.v7.services.GoogleAdsServiceClient;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.castled.ObjectRegistry;
import io.castled.apps.ExternalAppConnector;
import io.castled.apps.ExternalAppType;
import io.castled.apps.OAuthAppConfig;
import io.castled.apps.models.ExternalAppSchema;
import io.castled.apps.models.GenericSyncObject;
import io.castled.commons.models.AppSyncMode;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.forms.dtos.FormFieldOption;
import io.castled.oauth.OAuthDetails;
import io.castled.schema.SchemaConstants;
import io.castled.schema.models.RecordSchema;
import io.castled.services.OAuthService;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleAdsAppConnector implements ExternalAppConnector<GoogleAdsAppConfig, GoogleAdsDataSink, GoogleAdsAppSyncConfig> {

    private final OAuthService oAuthService;

    @Inject
    public GoogleAdsAppConnector(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @Override
    public List<FormFieldOption> getAllObjects(GoogleAdsAppConfig config, GoogleAdsAppSyncConfig mappingConfig) {
        return Arrays.stream(GAdsObjectType.values()).map(this::getFormSelectOption).collect(Collectors.toList());
    }

    @Override
    public List<FormFieldOption> getSubResources(GoogleAdsAppConfig config,
                                                 GoogleAdsAppSyncConfig mappingConfig) {
        GAdsObjectType gAdsObjectType = GAdsObjectType.valueOf(mappingConfig.getObject().getObjectName());
        List<GadsSubResource> gadsSyncObjects = getSyncObjects(config, gAdsObjectType, mappingConfig);
        return gadsSyncObjects.stream().map(gadsSyncObject ->
                new FormFieldOption(gadsSyncObject, gadsSyncObject.getObjectName())).collect(Collectors.toList());
    }

    @Override
    public List<AppSyncMode> getSyncModes(GoogleAdsAppConfig config, GoogleAdsAppSyncConfig appSyncConfig) {
        GAdsObjectType gAdsObjectType = GAdsObjectType.valueOf(appSyncConfig.getObject().getObjectName());
        switch (gAdsObjectType) {
            case CUSTOMER_MATCH:
                return Collections.singletonList(AppSyncMode.UPSERT);
            case CALL_CONVERSIONS:
            case CLICK_CONVERSIONS:
                return Collections.singletonList(AppSyncMode.INSERT);
            default:
                throw new CastledRuntimeException(String.format("Unhandled google ads object type %s", gAdsObjectType));
        }
    }


    @Override
    public ExternalAppSchema getSchema(GoogleAdsAppConfig config, GoogleAdsAppSyncConfig mappingConfig) {

        GAdsObjectType gAdsObjectType = GAdsObjectType.valueOf(mappingConfig.getObject().getObjectName());
        switch (gAdsObjectType) {
            case CUSTOMER_MATCH:
                return getSchemaForCustomerMatch(mappingConfig.getSubResource());
            case CLICK_CONVERSIONS:
                return getSchemaForClickConversions(config, mappingConfig);
            case CALL_CONVERSIONS:
                return getSchemaForCallConversions(config, mappingConfig);
        }

        throw new CastledRuntimeException(String.format("Unhandled object type %s", gAdsObjectType));
    }


    private Pair<String, String> getTitleAndDescription(GAdsObjectType gAdsObjectType) {
        switch (gAdsObjectType) {
            case CUSTOMER_MATCH:
                return Pair.of("Customer match list", "Sync customized users list to the customer match list to be used in ad campaigns");
            case CALL_CONVERSIONS:
                return Pair.of("Call Conversions", "Sync call conversion metrics to the call conversions object for better evaluation of your ad campaigns");
            case CLICK_CONVERSIONS:
                return Pair.of("Click Conversions", "Sync click conversion metrics to the click conversions object for better evaluation of your ad campaigns");
            default:
                throw new CastledRuntimeException(String.format("Invalid google ads object type %s", gAdsObjectType));
        }
    }

    private List<GadsSubResource> getSyncObjects(GoogleAdsAppConfig config, GAdsObjectType gAdsObjectType,
                                                 GoogleAdsAppSyncConfig mappingConfig) {

        OAuthDetails oAuthDetails = this.oAuthService.getOAuthDetails(config.getOAuthToken());
        GoogleAdsClient googleAdsClient = GoogleAdsClient.newBuilder().fromProperties(
                GoogleAdUtils.getClientProperties(config, oAuthDetails.getAccessConfig().getRefreshToken(), mappingConfig.getLoginCustomerId())).build();

        try (GoogleAdsServiceClient googleAdsServiceClient = googleAdsClient.getLatestVersion().createGoogleAdsServiceClient()) {
            switch (gAdsObjectType) {
                case CUSTOMER_MATCH:
                    return getCustomerMatchObjects(config, googleAdsServiceClient, mappingConfig);
                case CLICK_CONVERSIONS:
                    return getConversionObjects(googleAdsServiceClient, mappingConfig, gAdsObjectType);
                case CALL_CONVERSIONS:
                    return getConversionObjects(googleAdsServiceClient, mappingConfig, gAdsObjectType);
                default:
                    throw new CastledRuntimeException(String.format("Invalid google ads object type %s", gAdsObjectType));
            }
        }
    }

    private FormFieldOption getFormSelectOption(GAdsObjectType gAdsObjectType) {
        GenericSyncObject syncObject = new GenericSyncObject(gAdsObjectType.name(), ExternalAppType.GOOGLEADS);
        Pair<String, String> titleAndDescription = getTitleAndDescription(gAdsObjectType);
        return new FormFieldOption(syncObject, titleAndDescription.getLeft(), titleAndDescription.getRight());
    }


    private List<GadsSubResource> getConversionObjects(GoogleAdsServiceClient googleAdsServiceClient,
                                                       GoogleAdsAppSyncConfig mappingConfig, GAdsObjectType gAdsObjectType) {
        GoogleAdsServiceClient.SearchPagedResponse searchPagedResponse = googleAdsServiceClient
                .search(String.valueOf(mappingConfig.getAccountId()),
                        "SELECT conversion_action.name, conversion_action.resource_name,conversion_action.type FROM conversion_action");
        List<GadsSubResource> conversionActionObjects = Lists.newArrayList();
        for (GoogleAdsRow googleAdsRow : searchPagedResponse.iterateAll()) {
            ConversionAction conversionAction = googleAdsRow.getConversionAction();
            if (conversionAction.getType() == ConversionActionTypeEnum.ConversionActionType.UPLOAD_CALLS &&
                    gAdsObjectType == GAdsObjectType.CALL_CONVERSIONS) {
                conversionActionObjects.add(new GadsSubResource(conversionAction.getName(), conversionAction.getResourceName()));
            }
            if (conversionAction.getType() == ConversionActionTypeEnum.ConversionActionType.UPLOAD_CLICKS &&
                    gAdsObjectType == GAdsObjectType.CLICK_CONVERSIONS) {
                conversionActionObjects.add(new GadsSubResource(conversionAction.getName(), conversionAction.getResourceName()));
            }
        }
        return conversionActionObjects;
    }

    private List<GadsSubResource> getCustomerMatchObjects(OAuthAppConfig googleAdsAppConfig, GoogleAdsServiceClient
            googleAdsServiceClient,
                                                          GoogleAdsAppSyncConfig mappingConfig) {
        GoogleAdsServiceClient.SearchPagedResponse searchPagedResponse = googleAdsServiceClient
                .search(String.valueOf(mappingConfig.getAccountId()),
                        "SELECT user_list.name, user_list.id, user_list.resource_name," +
                                "user_list.crm_based_user_list.upload_key_type FROM user_list");

        List<GadsSubResource> userLists = Lists.newArrayList();
        for (GoogleAdsRow googleAdsRow : searchPagedResponse.iterateAll()) {
            CustomerMatchType customerMatchType =
                    getCustomerMatchType(googleAdsRow.getUserList().getCrmBasedUserList().getUploadKeyType());
            if (customerMatchType != null) {
                userLists.add(new GadsSubResource(customerMatchType, googleAdsRow.getUserList().getName(),
                        googleAdsRow.getUserList().getResourceName()));
            }
        }
        return userLists;

    }

    private CustomerMatchType getCustomerMatchType(CustomerMatchUploadKeyTypeEnum.CustomerMatchUploadKeyType
                                                           uploadKeyType) {
        if (uploadKeyType == CustomerMatchUploadKeyTypeEnum.CustomerMatchUploadKeyType.CONTACT_INFO) {
            return CustomerMatchType.CONTACT_INFO;
        }

        if (uploadKeyType == CustomerMatchUploadKeyTypeEnum.CustomerMatchUploadKeyType.CRM_ID) {
            return CustomerMatchType.CRM_ID;
        }

        if (uploadKeyType == CustomerMatchUploadKeyTypeEnum.CustomerMatchUploadKeyType.MOBILE_ADVERTISING_ID) {
            return CustomerMatchType.MOBILE_ADVERTISING_ID;
        }
        return null;
    }

    public List<AppSyncMode> getSyncModes(GenericSyncObject object, GadsSubResource subResource, OAuthAppConfig
            config) {
        return Lists.newArrayList(AppSyncMode.UPSERT);
    }

    @Override
    public GoogleAdsDataSink getDataSink() {
        return ObjectRegistry.getInstance(GoogleAdsDataSink.class);
    }

    private ExternalAppSchema getSchemaForCallConversions(GoogleAdsAppConfig googleAdsAppConfig,
                                                          GoogleAdsAppSyncConfig mappingConfig) {
        RecordSchema.Builder customerSchemaBuilder = RecordSchema.builder();
        for (GadsObjectFields.CALL_CONVERSION_STANDARD_FIELDS field : GadsObjectFields.CALL_CONVERSION_STANDARD_FIELDS.values()) {
            customerSchemaBuilder.put(field.getFieldName(), field.getSchema());
        }
        for (String customVariable : GoogleAdUtils.getCustomVariables(googleAdsAppConfig, mappingConfig).stream().map(ConversionCustomVariable::getName).collect(Collectors.toList())) {
            customerSchemaBuilder.put(customVariable, SchemaConstants.OPTIONAL_STRING_SCHEMA);
        }
        return new ExternalAppSchema(customerSchemaBuilder.build(), Lists.newArrayList(GadsObjectFields.CALL_CONVERSION_STANDARD_FIELDS.CALLER_ID.getFieldName()));
    }


    private ExternalAppSchema getSchemaForClickConversions(GoogleAdsAppConfig googleAdsAppConfig,
                                                           GoogleAdsAppSyncConfig googleAdsMappingConfig) {
        RecordSchema.Builder recordSchemaBuilder = RecordSchema.builder();
        for (GadsObjectFields.CLICK_CONVERSION_STANDARD_FIELDS field : GadsObjectFields.CLICK_CONVERSION_STANDARD_FIELDS.values()) {
            recordSchemaBuilder.put(field.getFieldName(), field.getSchema());
        }
        for (String customVariable : GoogleAdUtils.getCustomVariables(googleAdsAppConfig, googleAdsMappingConfig).stream().map(ConversionCustomVariable::getName).collect(Collectors.toList())) {
            recordSchemaBuilder.put(customVariable, SchemaConstants.OPTIONAL_STRING_SCHEMA);
        }
        return new ExternalAppSchema(recordSchemaBuilder.build(), Lists.newArrayList(GadsObjectFields.CLICK_CONVERSION_STANDARD_FIELDS.GCLID.getFieldName()));
    }

    private ExternalAppSchema getSchemaForCustomerMatch(GadsSubResource customerMatchSyncObject) {
        switch (customerMatchSyncObject.getCustomerMatchType()) {
            case CONTACT_INFO:
                RecordSchema.Builder customerSchemaBuilder = RecordSchema.builder();
                for (GadsObjectFields.CUSTOMER_MATCH_CONTACT_INFO_FIELDS customerMatchField : GadsObjectFields.CUSTOMER_MATCH_CONTACT_INFO_FIELDS.values()) {
                    customerSchemaBuilder.put(customerMatchField.getFieldName(), SchemaConstants.OPTIONAL_STRING_SCHEMA);
                }
                List<String> pkEligibles = Lists.newArrayList(GadsObjectFields.CUSTOMER_MATCH_CONTACT_INFO_FIELDS.EMAIL.getFieldName(),
                        GadsObjectFields.CUSTOMER_MATCH_CONTACT_INFO_FIELDS.PHONE_NUMBER.getFieldName());

                return new ExternalAppSchema(customerSchemaBuilder.build(), pkEligibles);

            case CRM_ID:
                RecordSchema.Builder userIdSchemaBuilder = RecordSchema.builder()
                        .put(GadsObjectFields.CUSTOMER_MATCH_USER_ID_FIELD, SchemaConstants.STRING_SCHEMA);
                return new ExternalAppSchema(userIdSchemaBuilder.build(), Lists.newArrayList(GadsObjectFields.CUSTOMER_MATCH_USER_ID_FIELD));

            case MOBILE_ADVERTISING_ID:
                RecordSchema.Builder mobileIdSchemaBuilder = RecordSchema.builder()
                        .put(GadsObjectFields.CUSTOMER_MATCH_MOBILE_DEVICE_ID_FIELD, SchemaConstants.STRING_SCHEMA);

                return new ExternalAppSchema(mobileIdSchemaBuilder.build(), Lists.newArrayList(GadsObjectFields.CUSTOMER_MATCH_MOBILE_DEVICE_ID_FIELD));

            default:
                throw new CastledRuntimeException(String.format("Invalid customer match type %s", customerMatchSyncObject.getCustomerMatchType()));

        }


    }

    public Class<GoogleAdsAppSyncConfig> getMappingConfigType() {
        return GoogleAdsAppSyncConfig.class;
    }

    @Override
    public Class<GoogleAdsAppConfig> getAppConfigType() {
        return GoogleAdsAppConfig.class;
    }


}
