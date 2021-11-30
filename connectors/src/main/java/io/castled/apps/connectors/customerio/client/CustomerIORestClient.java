package io.castled.apps.connectors.customerio.client;

import com.google.common.collect.Lists;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.customerio.CustomerIOObjectFields;
import io.castled.apps.connectors.customerio.dto.CustomerUpdateResponse;
import io.castled.apps.connectors.customerio.dto.EventDetails;
import io.castled.apps.connectors.customerio.dto.EventInsertResponse;
import io.castled.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
public class CustomerIORestClient {

    public static final String PAGE_EVENT = "page";
    public static final String NORMAL_EVENT = "event";
    public static final String BASIC_AUTH = "Basic ";
    private final Client client;


    private String siteId = null;
    private String apiKey = null;

    public static final String EVENT_URL = "https://track.customer.io/api/v1/customers/%s/events";
    public static final String CUSTOMER_URL = "https://track.customer.io/api/v1/customers/";


    public CustomerIORestClient(String siteId, String apiKey) {
        this.apiKey= apiKey;
        this.siteId = siteId;

        this.client = ObjectRegistry.getInstance(Client.class);
    }

    //Returns the list of failed records;
    public void upsertPersonDetails(Map<String,Object> personDetail, List<String> primaryKeys) {

        String pk = primaryKeys.stream().filter(e->e.equalsIgnoreCase("email")).findFirst().orElse("id");
        String pkValue = (String) personDetail.get(pk);
        personDetail.remove(pk);

        this.client.target(CUSTOMER_URL)
                .path(pkValue)
                .request(MediaType.APPLICATION_JSON).header(RestUtils.AUTHORIZATION_HEADER, getBasicHeader())
                .put(Entity.json(personDetail), String.class);
    }

    public void insertEventDetails(Map<String,Object> eventDetails, List<String> primaryKeys) {
        String customerId = (String) eventDetails.get(CustomerIOObjectFields.EVENT_FIELDS.CUSTOMER_ID.getFieldName());

        this.client.target(String.format(EVENT_URL, customerId))
                .request(MediaType.APPLICATION_JSON).header(RestUtils.AUTHORIZATION_HEADER, getBasicHeader())
                .post(Entity.json(constructEventDetails(eventDetails)), String.class);
    }


    private EventDetails constructEventDetails(Map<String,Object> eventDetails) {
        String eventName = (String) eventDetails.get(CustomerIOObjectFields.EVENT_FIELDS.EVENT_NAME.getFieldName());
        String pageUrl= (String) eventDetails.get(CustomerIOObjectFields.EVENT_FIELDS.PAGE_URL.getFieldName());
        String timestamp= (String) eventDetails.get(CustomerIOObjectFields.EVENT_FIELDS.EVENT_TIMESTAMP.getFieldName());

        eventDetails.keySet().removeAll(Lists.newArrayList(CustomerIOObjectFields.EVENT_FIELDS.EMAIL.getFieldName(),
                CustomerIOObjectFields.EVENT_FIELDS.CUSTOMER_ID.getFieldName(),
                CustomerIOObjectFields.EVENT_FIELDS.EVENT_NAME.getFieldName(),
                CustomerIOObjectFields.EVENT_FIELDS.EVENT_ID.getFieldName(),
                CustomerIOObjectFields.EVENT_FIELDS.EVENT_TIMESTAMP.getFieldName()));

        return EventDetails.builder().name(eventName!=null?eventName:pageUrl).type(pageUrl!=null? PAGE_EVENT : NORMAL_EVENT).data(eventDetails).timestamp(timestamp).build();
    }

    private String getBasicHeader() {
        return BASIC_AUTH + Base64.getEncoder().encodeToString((siteId+":"+apiKey).getBytes(StandardCharsets.UTF_8));
    }
}
