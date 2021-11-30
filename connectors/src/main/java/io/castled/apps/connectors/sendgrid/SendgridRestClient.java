package io.castled.apps.connectors.sendgrid;

import com.google.common.collect.Lists;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.sendgrid.dtos.*;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class SendgridRestClient {

    private static final String API_ENDPOINT = "https://api.sendgrid.com/v3/";

    private final Client client;
    private final SendgridAppConfig appConfig;

    public SendgridRestClient(SendgridAppConfig appConfig) {
        this.client = ObjectRegistry.getInstance(Client.class);
        this.appConfig = appConfig;
    }

    public ContactAttributesResponse getContactAttributes() {
        return this.client.target(String.format("%s/marketing/field_definitions", API_ENDPOINT))
                .request(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + appConfig.getApiKey())
                .get(ContactAttributesResponse.class);
    }

    public List<ContactList> getContactLists() {
        List<ContactList> contactLists = Lists.newArrayList();
        String currentUrl = String.format("%s/marketing/lists", API_ENDPOINT);
        while (currentUrl != null) {
            ContactListsResponse response = this.client.target(String.format("%s/marketing/lists", API_ENDPOINT))
                    .request(MediaType.APPLICATION_JSON)
                    .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + appConfig.getApiKey())
                    .get(ContactListsResponse.class);
            contactLists.addAll(response.getResult());
            currentUrl = response.getMetadata().getNext();
        }
        return contactLists;
    }

    public List<SendgridUpsertError> upsertContacts(List<Map<String, Object>> contacts, String listId) {
        BatchUpsertContactRequest batchRequest = constructBatchContactRequest(contacts, listId);
        BatchUpsertJobIdResponse batchUpsertResponse = this.client.target(String.format("%s/marketing/contacts", API_ENDPOINT))
                .request(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + this.appConfig.getApiKey())
                .put(Entity.json(batchRequest), BatchUpsertJobIdResponse.class);
        BatchUpsertStatusResponse statusResponse;
        do {
            ThreadUtils.interruptIgnoredSleep(TimeUtils.secondsToMillis(2));
            statusResponse = getBatchStatus(batchUpsertResponse.getJobId());
        } while (BatchUpsertStatus.PENDING.equals(statusResponse.getStatus()));
        List<SendgridUpsertError> upsertErrors = Lists.newArrayList();
        if (BatchUpsertStatus.ERRORED.equals(statusResponse.getStatus()) || BatchUpsertStatus.FAILED.equals(statusResponse.getStatus())) {
            upsertErrors = createErrorResponse(statusResponse);
        }
        return upsertErrors;
    }

    private BatchUpsertContactRequest constructBatchContactRequest(List<Map<String, Object>> contacts, String listId) {
        // TODO: listId needs to be an array of list ids once multiselect for listids available in UI.
        List<String> listIds = Lists.newArrayList();
        Optional.ofNullable(listId).map(listRef -> listIds.add(listRef));
        BatchUpsertContactRequest batchContactRequest = new BatchUpsertContactRequest(listIds, contacts);
        return batchContactRequest;
    }

    private List<SendgridUpsertError> createErrorResponse(BatchUpsertStatusResponse response) {
        final String[] CSV_HEADER = {
                "primary_email",
                "contact_id",
                "errors_occurred"
        };
        InputStream errorStream = this.client.target(response.getResults().getErrorsUrl())
                .request(MediaType.APPLICATION_OCTET_STREAM)
                .get(InputStream.class);
        List<SendgridUpsertError> upsertErrors = Lists.newArrayList();
        try {
            CSVParser csvParser = new CSVParser(new InputStreamReader(errorStream), CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim());
            for (CSVRecord csvRecord : csvParser) {
                String email = csvRecord.get(CSV_HEADER[0]);
                // skipping contact_id
                String message = csvRecord.get(CSV_HEADER[2]);
                upsertErrors.add(new SendgridUpsertError(email, message));
            }
        } catch (IOException e) {
            log.error(String.format("Creating error response from url:%s failed", response.getResults().getErrorsUrl()), e);
            throw new CastledRuntimeException(e);
        }
        return upsertErrors;
    }

    private BatchUpsertStatusResponse getBatchStatus(String jobId) {
        return this.client.target(String.format("%s/marketing/contacts/imports/%s", API_ENDPOINT, jobId))
                .request(MediaType.APPLICATION_JSON).header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + this.appConfig.getApiKey())
                .get(BatchUpsertStatusResponse.class);
    }
}