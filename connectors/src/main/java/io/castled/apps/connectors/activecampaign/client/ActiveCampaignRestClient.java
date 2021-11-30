package io.castled.apps.connectors.activecampaign.client;

import io.castled.ObjectRegistry;
import io.castled.apps.connectors.activecampaign.constant.ActiveCampaignConstants;
import io.castled.apps.connectors.activecampaign.dto.*;
import io.castled.apps.connectors.activecampaign.models.ContactAndError;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.utils.JsonUtils;
import io.castled.utils.ThreadUtils;
import io.castled.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
public class ActiveCampaignRestClient {

    private final Client client;


    private String apiURL = null;
    private String apiKey = null;


    public ActiveCampaignRestClient(String apiURL, String apiKey) {
        this.apiKey= apiKey;
        this.apiURL = apiURL;

        this.client = ObjectRegistry.getInstance(Client.class);
    }

    public List<CustomDataAttribute> getContactCustomFields() {
        return this.client.target(String.format(ActiveCampaignConstants.CUSTOM_FIELD_FETCH_URL, this.apiURL, ActiveCampaignConstants.API_VERSION))
                .request(MediaType.APPLICATION_JSON).header(ActiveCampaignConstants.API_TOKEN_HEADER ,this.apiKey)
                .get(CustomDataAttributeResponse.class).getFields();

    }

    //Returns the list of failed records;
    public List<ContactAndError> upsertContacts(List<Contact> contacts) {

        List<ContactAndError> contactAndErrors = new ArrayList<>();
        boolean isRunCompleted = false ;

        while(!isRunCompleted)
        {
            try {
                BulkImportResponse bulkImportResponse = invokeBulkImport(contacts);
                Integer responseType = bulkImportResponse.getSuccess();
                log.info("Response : "+responseType);
                isRunCompleted = responseType == 1 ?
                        handleSuccessResponse(contacts, contactAndErrors, bulkImportResponse):handleFailureResponse(contacts, contactAndErrors, bulkImportResponse);
            }
            catch (BadRequestException badRequestException) {
                log.error("Upsert failed : ", badRequestException);
                contactAndErrors.addAll(contacts.stream().map(contact -> new ContactAndError(contact, Collections.singletonList(badRequestException.getMessage()))).collect(Collectors.toList()));
            }
            catch (Exception e) {
                log.error("Upsert failed", e);
                throw new CastledRuntimeException(e);
            }
        }

        return contactAndErrors;
    }

    private boolean handleFailureResponse(List<Contact> contacts, List<ContactAndError> contactAndErrors, BulkImportResponse bulkImportResponse) {
        List<FailureReason> failureReasons = bulkImportResponse.getFailureReasons();
        Map<Integer,List<FailureReason>> contactIndexToFailureReasonsMap = failureReasons.stream().filter(failureReason -> failureReason.getContact()!=null).collect(groupingBy(FailureReason::getContact));
        contactAndErrors.addAll(contactIndexToFailureReasonsMap.entrySet().stream().
                map(failedReason -> new ContactAndError(contacts.get(failedReason.getKey()),failedReason.getValue().stream().map(FailureReason::getFailureReason).collect(Collectors.toList()))).
                collect(Collectors.toList()));

        List<Contact> failedContacts = contactAndErrors.stream().map(ContactAndError::getContact).collect(Collectors.toList());
        contacts.removeAll(failedContacts);
        return CollectionUtils.isEmpty(contacts);
    }

    private boolean handleSuccessResponse(List<Contact> contacts, List<ContactAndError> contactAndErrors, BulkImportResponse bulkImportResponse) {
        List<String> failures = new ArrayList<>();
        BulkImportStatusInfoResponse bulkImportStatusInfoResponse = null;
        do {
            ThreadUtils.interruptIgnoredSleep(TimeUtils.secondsToMillis(5));
            log.info("Batch id :"+bulkImportResponse.getBatchId());
            bulkImportStatusInfoResponse = invokeBatchImportStatusCheck(bulkImportResponse.getBatchId());
            failures.addAll(new ArrayList<>(bulkImportStatusInfoResponse.getFailure()));
        }
        while (!bulkImportStatusInfoResponse.getStatus().equalsIgnoreCase("completed"));

        contactAndErrors.addAll(failures.stream().
                map( failure -> new ContactAndError(contacts.get(Integer.parseInt(failure)), Collections.singletonList(bulkImportResponse.getMessage()))).collect(Collectors.toList()));

        return true;
    }

    private BulkImportResponse invokeBulkImport(List<Contact> contacts)
    {
        String response =  this.client.target(String.format(ActiveCampaignConstants.BULK_IMPORT_URL,
                this.apiURL, ActiveCampaignConstants.API_VERSION))
                .request(MediaType.APPLICATION_JSON).header(ActiveCampaignConstants.API_TOKEN_HEADER,this.apiKey)
                .post(Entity.json(new BulkImportRequest(contacts)), String.class);

        return JsonUtils.jsonStringToObject(response, BulkImportResponse.class);
    }

    private BulkImportStatusInfoResponse invokeBatchImportStatusCheck(String batchId){
        String statusCheck =  this.client.target(String.format(ActiveCampaignConstants.BULK_IMPORT_INFO_URL,
                this.apiURL, ActiveCampaignConstants.API_VERSION)).queryParam(ActiveCampaignConstants.QUERY_PARAM_BATCH_ID,batchId)
                .request(MediaType.APPLICATION_JSON).header(ActiveCampaignConstants.API_TOKEN_HEADER,this.apiKey)
                .get(String.class);

        return JsonUtils.jsonStringToObject(statusCheck,BulkImportStatusInfoResponse.class);
    }
}
