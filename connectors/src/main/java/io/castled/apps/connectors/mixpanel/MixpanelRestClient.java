package io.castled.apps.connectors.mixpanel;

import io.castled.ObjectRegistry;
import io.castled.apps.connectors.mixpanel.dto.*;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.utils.JsonUtils;
import io.castled.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
public class MixpanelRestClient {

    public static final String BASIC_AUTH = "Basic ";
    private final Client client;


    private String projectToken = null;
    private String apiSecret = null;

    public static final String EVENT_URL = "https://api.mixpanel.com/import";
    public static final String USER_PROFILE_URL = "https://api.mixpanel.com/engage#profile-batch-update";
    public static final String GROUP_PROFILE_URL = "https://api.mixpanel.com/groups#group-batch-update";


    public MixpanelRestClient(String projectToken, String apiSecret) {
        this.apiSecret= apiSecret;
        this.projectToken = projectToken;

        this.client = ObjectRegistry.getInstance(Client.class);
    }

    public List<UserProfileAndError> upsertUserProfileDetails(List<Map<String,Object>> userProfileDetails) {

        List<UserProfileAndError> userProfileAndErrors = new ArrayList<>();
        try {
            ProfileBulkUpdateResponse profileBulkUpdateResponse = invokeBulkUserProfileUpsert(userProfileDetails);
            log.info("Userprofile bulk update Response : "+profileBulkUpdateResponse.getStatus());

            if("0".equalsIgnoreCase(profileBulkUpdateResponse.getStatus())) {
                handleUserProfileUpsertFailure(profileBulkUpdateResponse,userProfileAndErrors,userProfileDetails);
            }
        }
        catch (BadRequestException badRequestException) {
            log.error("Userprofile bulk upsert failed : ", badRequestException);
            userProfileAndErrors.addAll(userProfileDetails.stream().map(contact -> new UserProfileAndError((String) contact.get("$"+MixpanelObjectFields.USER_PROFILE_FIELDS.DISTINCT_ID.getFieldName()),
                    Collections.singletonList(badRequestException.getMessage()))).collect(Collectors.toList()));
        }
        catch (Exception e) {
            log.error("Upsert failed", e);
            throw new CastledRuntimeException(e);
        }

        return userProfileAndErrors;
    }

    public List<GroupProfileAndError> upsertGroupProfileDetails(List<Map<String,Object>> groupProfileDetails) {

        List<GroupProfileAndError> groupProfileAndErrors = new ArrayList<>();
        try {
            ProfileBulkUpdateResponse response = invokeBulkGroupProfileUpsert(groupProfileDetails);
            log.info("Group profile Response : "+ response.getStatus());
            if("0".equalsIgnoreCase(response.getStatus())) {
                handleGroupProfileUpsertFailure(response,groupProfileAndErrors,groupProfileDetails);
            }
        }
        catch (BadRequestException badRequestException) {
            log.error("Group profile bulk update failed: ", badRequestException);
            groupProfileAndErrors.addAll(groupProfileDetails.stream().map(contact -> new GroupProfileAndError((String) contact.get("$"+MixpanelObjectFields.GROUP_PROFILE_FIELDS.GROUP_ID.getFieldName()),
                    Collections.singletonList(badRequestException.getMessage()))).collect(Collectors.toList()));
        }
        catch (Exception e) {
            log.error("Group profile bulk update failed: ", e);
            throw new CastledRuntimeException(e);
        }
        return groupProfileAndErrors;
    }

    public List<EventAndError> insertEventDetails(List<Map<String,Object>> userProfileDetails) {

        List<EventAndError> eventAndErrors = new ArrayList<>();
        try {
            EventBulkInsertResponse bulkImportResponse = invokeBulkEventInsert(userProfileDetails);
            log.info("Response for Event Bulk inserts : "+bulkImportResponse.getCode());

            if(!"200".equalsIgnoreCase(bulkImportResponse.getCode())) {
                handleFailure(bulkImportResponse,eventAndErrors,userProfileDetails);
            }
        }
        catch (BadRequestException badRequestException) {
            log.error("Event bulk insert failed because of BAD REQUEST : ", badRequestException);
            eventAndErrors.addAll(userProfileDetails.stream().map(userDetail -> new EventAndError(0,
                    (String) userDetail.get("$"+MixpanelObjectFields.EVENT_FIELDS.INSERT_ID), Collections.singletonList(badRequestException.getMessage()))).collect(Collectors.toList()));
        }
        catch (Exception e) {
            log.error("Event bulk insert failed : ", e);
            eventAndErrors.addAll(userProfileDetails.stream().map(userDetail -> new EventAndError(0,
                    (String) userDetail.get("$"+MixpanelObjectFields.EVENT_FIELDS.INSERT_ID), Collections.singletonList(e.getMessage()))).collect(Collectors.toList()));
            throw new CastledRuntimeException(e);
        }
        return eventAndErrors;
    }

    private boolean handleUserProfileUpsertFailure(ProfileBulkUpdateResponse profileBulkUpdateResponse, List<UserProfileAndError> groupProfileAndErrors, List<Map<String, Object>> groupProfileDetails) {
        groupProfileAndErrors.addAll(groupProfileDetails.stream().
                map(event -> new UserProfileAndError( (String) event.get("$"+MixpanelObjectFields.USER_PROFILE_FIELDS.DISTINCT_ID.getFieldName()), Collections.singletonList(profileBulkUpdateResponse.getError())))
                .collect(Collectors.toList()));
        return true;
    }

    private boolean handleGroupProfileUpsertFailure(ProfileBulkUpdateResponse profileBulkUpdateResponse, List<GroupProfileAndError> groupProfileAndErrors, List<Map<String, Object>> groupProfileDetails) {
        groupProfileAndErrors.addAll(groupProfileDetails.stream().
                map(event -> new GroupProfileAndError( (String) event.get("$"+MixpanelObjectFields.GROUP_PROFILE_FIELDS.GROUP_ID.getFieldName()), Collections.singletonList(profileBulkUpdateResponse.getError())))
                .collect(Collectors.toList()));
        return true;
    }

    private void handleFailure(EventBulkInsertResponse bulkImportResponse, List<EventAndError> eventAndErrorList, List<Map<String, Object>> eventDetails) {

        Integer numberOfRecordsSynced = bulkImportResponse.getNum_records_imported();
        Integer failedRecordCount = eventDetails.size()-numberOfRecordsSynced;

        String code = bulkImportResponse.getCode();
        switch(code){
            //Some records failed validation
            case "400": {
                handleErrorCode400(bulkImportResponse,eventAndErrorList,eventDetails);
            }
            //Invalid credentials
            case "401":{
                handleErrorCode401(bulkImportResponse,eventAndErrorList,eventDetails);
            }
            //payload is large
            case "413" : {
                handleErrorCode413(bulkImportResponse,eventAndErrorList,eventDetails);
            }
            //Rate limit exceeded
            case "429" : {
                handleErrorCode429(bulkImportResponse,eventAndErrorList,eventDetails);
            }
        }
    }

    private boolean handleErrorCode413(EventBulkInsertResponse bulkImportResponse, List<EventAndError> eventAndErrorList, List<Map<String, Object>> eventDetails){
        return true;
    }

    private boolean handleErrorCode429(EventBulkInsertResponse bulkImportResponse, List<EventAndError> eventAndErrorList, List<Map<String, Object>> eventDetails){
        return true;
    }

    private boolean handleErrorCode401(EventBulkInsertResponse bulkImportResponse, List<EventAndError> eventAndErrorList, List<Map<String, Object>> eventDetails){

        eventAndErrorList.addAll(eventDetails.stream().
                map(event -> new EventAndError(0, (String) event.get("$"+MixpanelObjectFields.EVENT_FIELDS.INSERT_ID), Collections.singletonList("Invalid Credentials")))
                .collect(Collectors.toList()));
        return true;
    }

    private boolean handleErrorCode400(EventBulkInsertResponse bulkImportResponse, List<EventAndError> eventAndErrorList, List<Map<String, Object>> eventDetails){

        List<EventInsertFailureRecord> failureReasons = bulkImportResponse.getFailed_records();

        Map<Integer,List<EventInsertFailureRecord>> contactIndexToFailureReasonsMap = failureReasons.stream().
                filter(failureReason -> failureReason.getIndex()!=null).collect(groupingBy(EventInsertFailureRecord::getIndex));
        eventAndErrorList.addAll(contactIndexToFailureReasonsMap.entrySet().stream()
                .map(failedReason -> new EventAndError(failedReason.getKey(),(String) eventDetails.get(failedReason.getKey()).get("$insert_id"),
                        failedReason.getValue().stream().map(EventInsertFailureRecord::getMessage).collect(Collectors.toList())))
                .collect(Collectors.toList()));

        List<Integer> failedContacts = eventAndErrorList.stream().map(EventAndError::getIndex).collect(Collectors.toList());
        failedContacts.stream().forEach(index->eventDetails.remove(index));

        return true;
    }

    private ProfileBulkUpdateResponse invokeBulkUserProfileUpsert(List<Map<String, Object>> userProfileDetails) {
        Form form = new Form();
        form.param("data",JsonUtils.objectToString(userProfileDetails));
        form.param("verbose", "1");

        return this.client.target(USER_PROFILE_URL)
                .request(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .post(Entity.form(form),ProfileBulkUpdateResponse.class);
    }

    private ProfileBulkUpdateResponse invokeBulkGroupProfileUpsert(List<Map<String, Object>> groupProfileDetails) {
        Form form = new Form();
        form.param("data",JsonUtils.objectToString(groupProfileDetails));
        form.param("verbose", "1");

        return this.client.target(GROUP_PROFILE_URL)
                .request(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .post(Entity.form(form),ProfileBulkUpdateResponse.class);
    }

    private EventBulkInsertResponse invokeBulkEventInsert(List<Map<String, Object>> userProfileDetails) {
        return this.client.target(String.format(EVENT_URL))
                .queryParam("strict", 1)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, getBasicHeader())
                .post(Entity.json(userProfileDetails), EventBulkInsertResponse.class);
    }

    private String getBasicHeader() {
        return BASIC_AUTH + Base64.getEncoder().encodeToString((apiSecret+":"+" ").getBytes(StandardCharsets.UTF_8));
    }
}
