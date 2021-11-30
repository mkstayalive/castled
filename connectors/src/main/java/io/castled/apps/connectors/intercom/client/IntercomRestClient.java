package io.castled.apps.connectors.intercom.client;

import com.google.common.collect.Maps;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.intercom.client.dtos.*;
import io.castled.apps.connectors.intercom.client.exceptions.IntercomRestException;
import io.castled.apps.connectors.intercom.client.models.IntercomModel;
import io.castled.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class IntercomRestClient {

    private final Client client;
    private final String accessToken;

    public IntercomRestClient(String accessToken) {
        this.client = ObjectRegistry.getInstance(Client.class);
        this.accessToken = accessToken;
    }

    public List<DataAttribute> listAttributes(IntercomModel intercomModel) {
        return this.client.target("https://api.intercom.io/data_attributes")
                .queryParam("model", intercomModel.getName())
                .request(MediaType.APPLICATION_JSON).header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .get(DataAttributesResponse.class).getData();
    }

    public void createContact(Map<String, Object> contactProperties, List<String> customAttributes) throws IntercomRestException {
        Map<String, Object> requestProperties = constructRequestObject(contactProperties, customAttributes);

        try (Response response = executeRequest(() -> this.client.target("https://api.intercom.io/contacts")
                .request(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .post(Entity.json(requestProperties)))) {
            //do nothing

        }
    }

    public void createOrUpdateContact(Map<String, Object> contactProperties, List<String> customAttributes) throws IntercomRestException {
        try {
            createContact(contactProperties, customAttributes);
        } catch (IntercomRestException e) {
            Matcher matcher = Pattern.compile("A contact matching those details already exists with id=(.*)").matcher(e.getMessage());
            if (matcher.find()) {
                String contactId = matcher.group(1);
                updateContact(contactId, contactProperties, customAttributes);
                return;
            }
            throw e;
        }
    }


    public void updateContact(String contactId, Map<String, Object> contactProperties, List<String> customAttributes) throws IntercomRestException {
        Map<String, Object> requestProperties = constructRequestObject(contactProperties, customAttributes);
        try (Response response = executeRequest(() -> this.client.target(String.format("https://api.intercom.io/contacts/%s", contactId))
                .request(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .put(Entity.json(requestProperties)))) {
        }
    }

    public void createCompany(Map<String, Object> companyProperties, List<String> customAttributes) throws IntercomRestException {
        Map<String, Object> requestProperties = constructRequestObject(companyProperties, customAttributes);
        try (Response response = executeRequest(() -> this.client.target("https://api.intercom.io/companies")
                .request(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .post(Entity.json(requestProperties)))) {
        }
    }

    public void updateCompany(String companyId, Map<String, Object> companyProperties, List<String> customAttributes) {
        //this is not used as update and create can happeven via the create api

    }

    public void consumeContacts(Consumer<Map<String, Object>> contactsConsumer) {
        PaginatedObjectList contactList = this.client.target("https://api.intercom.io/contacts")
                .queryParam("per_page", 150)
                .request(MediaType.APPLICATION_JSON).header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .get(PaginatedObjectList.class);
        contactList.getData().stream().map(this::flattenPropertyResponse).forEach(contactsConsumer);

        while (contactList.getPages().getNextOffset() != null) {
            contactList = this.client.target("https://api.intercom.io/contacts")
                    .queryParam("per_page", 150)
                    .queryParam("starting_after", contactList.getPages().getNextOffset())
                    .request(MediaType.APPLICATION_JSON).header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                    .get(PaginatedObjectList.class);
            contactList.getData().stream().map(this::flattenPropertyResponse).forEach(contactsConsumer);
        }
    }

    private Map<String, Object> flattenPropertyResponse(Map<String, Object> properties) {
        Map<String, Object> flattenedProperties = Maps.newHashMap();
        Map<String, Object> customProperties = (Map<String, Object>) properties.get("custom_attributes");
        flattenedProperties.putAll(properties);
        flattenedProperties.remove("custom_properties");
        flattenedProperties.putAll(customProperties);
        return flattenedProperties;
    }

    private Map<String, Object> constructRequestObject(Map<String, Object> properties, List<String> customAttributes) {
        Map<String, Object> requestProperties = Maps.newHashMap();
        Map<String, Object> customProperties = CollectionUtils.isNotEmpty(customAttributes)
                ? customAttributes.stream().filter(attribute -> properties.get(attribute) != null)
                .collect(Collectors.toMap(Function.identity(), properties::get)) : Maps.newHashMap();
        requestProperties.putAll(properties);
        if (MapUtils.isNotEmpty(customProperties)) {
            requestProperties.put("custom_attributes", customProperties);
        }
        customAttributes.forEach(requestProperties::remove);
        return requestProperties;
    }

    public void sendEvent(IntercomEvent intercomEvent) throws IntercomRestException {
        try (Response response = executeRequest(() -> this.client.target("https://api.intercom.io/events")
                .request(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .post(Entity.json(intercomEvent)))) {
        }
    }

    private Response executeRequest(Supplier<Response> supplier) throws IntercomRestException {
        return doExecuteRequest(supplier, 0, 3);
    }

    private Response doExecuteRequest(Supplier<Response> supplier, int retryCount, int maxRetries) throws IntercomRestException {
        Response response = supplier.get();
        if (!ResponseUtils.is2xx(response)) {
            String errorString = response.readEntity(String.class);
            IntercomErrorResponse errorResponse = JsonUtils.jsonStringToObject(errorString, IntercomErrorResponse.class);
            if (errorResponse.getFirstError().getCode().equals("rate_limit_exceeded") && retryCount < maxRetries) {
                ThreadUtils.interruptIgnoredSleep(TimeUtils.secondsToMillis(10));
                doExecuteRequest(supplier, retryCount + 1, maxRetries);
            }
            throw new IntercomRestException(errorResponse);
        }
        return response;

    }
}
