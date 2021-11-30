package io.castled.apps.connectors.googleads;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v7.common.CustomerMatchUserListMetadata;
import com.google.ads.googleads.v7.common.OfflineUserAddressInfo;
import com.google.ads.googleads.v7.common.UserData;
import com.google.ads.googleads.v7.common.UserIdentifier;
import com.google.ads.googleads.v7.enums.OfflineUserDataJobTypeEnum;
import com.google.ads.googleads.v7.resources.OfflineUserDataJob;
import com.google.ads.googleads.v7.services.*;
import io.castled.ObjectRegistry;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.oauth.OAuthDAO;
import io.castled.oauth.OAuthDetails;
import io.castled.schema.models.Message;
import io.castled.schema.models.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class CustomerMatchObjectSink extends GadsObjectSink {

    private final GoogleAdsAppSyncConfig mappingConfig;
    private final String offlineUserDataJobResource;
    private final OfflineUserDataJobServiceClient offlineUserDataJobServiceClient;

    public CustomerMatchObjectSink(GoogleAdsAppSyncConfig mappingConfig,
                                   GoogleAdsAppConfig googleAdsAppConfig, ErrorOutputStream errorOutputStream) {

        super(errorOutputStream);
        this.mappingConfig = mappingConfig;

        OAuthDetails oAuthDetails = ObjectRegistry.getInstance(Jdbi.class)
                .onDemand(OAuthDAO.class).
                getOAuthDetails(googleAdsAppConfig.getOAuthToken());
        GoogleAdsClient googleAdsClient = GoogleAdsClient.newBuilder()
                .fromProperties(GoogleAdUtils.getClientProperties(googleAdsAppConfig, oAuthDetails.getAccessConfig().getRefreshToken(), mappingConfig.getLoginCustomerId())).build();

        this.offlineUserDataJobServiceClient = googleAdsClient.getLatestVersion().createOfflineUserDataJobServiceClient();
        this.offlineUserDataJobResource = createOfflineUserDataJob();
    }

    private static String toSHA256String(String str) {
        MessageDigest digest = getSHA256MessageDigest();
        byte[] hash = digest.digest(toNormalizedString(str).getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private static MessageDigest getSHA256MessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Missing SHA-256 algorithm implementation.", e);
        }
    }

    private static String toNormalizedString(String value) {
        return value.trim().toLowerCase();
    }

    private String createOfflineUserDataJob() {
        OfflineUserDataJob offlineUserDataJob = OfflineUserDataJob.newBuilder()
                .setType(OfflineUserDataJobTypeEnum.OfflineUserDataJobType.CUSTOMER_MATCH_USER_LIST)
                .setCustomerMatchUserListMetadata(
                        CustomerMatchUserListMetadata.newBuilder().setUserList(mappingConfig.getSubResource().getResourceName()))
                .build();

        // Issues a request to create the offline user data job.
        CreateOfflineUserDataJobResponse createOfflineUserDataJobResponse =
                offlineUserDataJobServiceClient.createOfflineUserDataJob(
                        String.valueOf(mappingConfig.getAccountId()), offlineUserDataJob);
        return createOfflineUserDataJobResponse.getResourceName();

    }

    protected void writeRecords(List<Message> messages) {
        List<OfflineUserDataJobOperation> userDataJobOperations = messages.stream().map(this::getUserDataOperation)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        AddOfflineUserDataJobOperationsResponse response =
                offlineUserDataJobServiceClient.addOfflineUserDataJobOperations(
                        AddOfflineUserDataJobOperationsRequest.newBuilder()
                                .setResourceName(this.offlineUserDataJobResource).setEnablePartialFailure(true)
                                .addAllOperations(userDataJobOperations).build());

        if (response.hasPartialFailureError()) {
            handlePartialFailures(messages, response.getPartialFailureError());
        }
        this.processedRecords.addAndGet(messages.size());
        this.lastProcessedMessageId = Math.min(lastProcessedMessageId, messages.get(messages.size() - 1).getOffset());
    }

    private OfflineUserDataJobOperation getUserDataOperation(Message message) {

        UserIdentifier userIdentifier = getUserIdentifier(message.getRecord());
        if (userIdentifier == null) {
            return null;
        }
        return OfflineUserDataJobOperation.newBuilder()
                .setCreate(UserData.newBuilder().addUserIdentifiers(getUserIdentifier(message.getRecord()))).build();
    }

    private UserIdentifier getUserIdentifier(Tuple record) {
        switch (mappingConfig.getSubResource().getCustomerMatchType()) {
            case CONTACT_INFO:
                return getContactInfoIdentifer(record);
            case MOBILE_ADVERTISING_ID:
                return getMobileDeviceIdentifier(record);
            case CRM_ID:
                return getThirdPartyUserIdentifier(record);
            default:
                throw new CastledRuntimeException(String.format("Unhandled customer match type %s", mappingConfig.getSubResource().getCustomerMatchType()));
        }

    }

    private UserIdentifier getContactInfoIdentifer(Tuple record) {
        String firstName = (String) record.getValue(GadsObjectFields.CUSTOMER_MATCH_CONTACT_INFO_FIELDS.FIRST_NAME.getFieldName());
        String lastName = (String) record.getValue(GadsObjectFields.CUSTOMER_MATCH_CONTACT_INFO_FIELDS.LAST_NAME.getFieldName());
        String countryCode = (String) record.getValue(GadsObjectFields.CUSTOMER_MATCH_CONTACT_INFO_FIELDS.COUNTRY_CODE.getFieldName());
        String email = (String) record.getValue(GadsObjectFields.CUSTOMER_MATCH_CONTACT_INFO_FIELDS.EMAIL.getFieldName());
        String postalCode = (String) record.getValue(GadsObjectFields.CUSTOMER_MATCH_CONTACT_INFO_FIELDS.POSTAL_CODE.getFieldName());
        String phoneNumber = (String) record.getValue(GadsObjectFields.CUSTOMER_MATCH_CONTACT_INFO_FIELDS.PHONE_NUMBER.getFieldName());

        OfflineUserAddressInfo.Builder userAddressBuilder = OfflineUserAddressInfo.newBuilder();
        Optional.ofNullable(firstName).ifPresent(fnRef -> userAddressBuilder.setHashedFirstName(toSHA256String(fnRef)));
        Optional.ofNullable(lastName).ifPresent(lnRef -> userAddressBuilder.setHashedLastName(toSHA256String(lnRef)));
        Optional.ofNullable(countryCode).ifPresent(userAddressBuilder::setCountryCode);
        Optional.ofNullable(postalCode).ifPresent(userAddressBuilder::setPostalCode);

        UserIdentifier.Builder userIdBuilder = UserIdentifier.newBuilder();

        userIdBuilder.setAddressInfo(userAddressBuilder.build());
        Optional.ofNullable(email).ifPresent(emailRef -> userIdBuilder.setHashedEmail(toSHA256String(emailRef)));

        Optional.ofNullable(phoneNumber).ifPresent(phoneRef -> userIdBuilder.setHashedPhoneNumber(toSHA256String(phoneRef)));

        return userIdBuilder.build();

    }

    private UserIdentifier getMobileDeviceIdentifier(Tuple record) {
        UserIdentifier.Builder userIdBuilder = UserIdentifier.newBuilder();
        String mobileDeviceId = (String) record.getValue(GadsObjectFields.CUSTOMER_MATCH_MOBILE_DEVICE_ID_FIELD);
        if (mobileDeviceId == null) {
            return null;
        }
        return userIdBuilder.setMobileId(mobileDeviceId).build();
    }

    private UserIdentifier getThirdPartyUserIdentifier(Tuple record) {
        UserIdentifier.Builder userIdBuilder = UserIdentifier.newBuilder();
        String thirdPartyUserId = (String) record.getValue(GadsObjectFields.CUSTOMER_MATCH_USER_ID_FIELD);
        if (thirdPartyUserId == null) {
            return null;
        }
        return userIdBuilder.setThirdPartyUserId(thirdPartyUserId).build();
    }

    @Override
    public void afterRecordsFlush() {
        offlineUserDataJobServiceClient.runOfflineUserDataJobAsync(this.offlineUserDataJobResource);
        this.offlineUserDataJobServiceClient.close();
    }

}
