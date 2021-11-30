package io.castled.apps.connectors.hubspot.client;

import com.google.common.collect.Lists;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.hubspot.HubspotAccessConfig;
import io.castled.apps.connectors.hubspot.client.dtos.*;
import io.castled.apps.connectors.hubspot.client.exception.BatchObjectException;
import io.castled.apps.connectors.hubspot.oauth.HubspotAccessTokenRefresher;
import io.castled.core.WaitTimeAndRetry;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.functionalinterfaces.ThrowingSupplier;
import io.castled.oauth.OAuthClientConfig;
import io.castled.oauth.OAuthDAO;
import io.castled.oauth.OAuthDetails;
import io.castled.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.function.Consumer;


@Slf4j
public class HubspotRestClient {

    private static final String HUBSPOT_BASE_URL = "https://api.hubapi.com";
    private final Client client;
    private final HubspotAccessTokenRefresher accessTokenRefresher;
    private final Long oauthToken;
    private HubspotAccessConfig hubspotAccessConfig;

    public HubspotRestClient(Long oauthToken, OAuthClientConfig oAuthClientConfig) {
        this.oauthToken = oauthToken;
        this.client = ObjectRegistry.getInstance(Client.class);
        OAuthDetails oAuthDetails = ObjectRegistry.getInstance(Jdbi.class)
                .onDemand(OAuthDAO.class).getOAuthDetails(oauthToken);
        this.hubspotAccessConfig = (HubspotAccessConfig) oAuthDetails.getAccessConfig();
        this.accessTokenRefresher = new HubspotAccessTokenRefresher(oAuthClientConfig);
    }

    public List<HubspotProperty> getObjectProperties(String objectType) {
        return executeRequest(() -> this.client.target(String.format("%s/properties/v2/%s/properties", HUBSPOT_BASE_URL, objectType))
                .request(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + hubspotAccessConfig.getAccessToken())
                .get(new GenericType<List<HubspotProperty>>() {
                }));
    }

    private void doUpdateObjects(String batchUpdateUrl, BatchUpdateRequest batchUpdateRequest, boolean create, int retries) throws BatchObjectException {

        String batchUrl = String.format("%s/crm/v3/objects/%s/batch/%s", HUBSPOT_BASE_URL, batchUpdateUrl, create ? "create" : "update");
        try (Response response = executeRequest(() -> this.client.target(batchUrl)
                .request(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + hubspotAccessConfig.getAccessToken())
                .post(Entity.json(batchUpdateRequest)))) {
            if (!ResponseUtils.is2xx(response)) {
                if (response.getStatus() == 502) {
                    if (retries < 3) {
                        ThreadUtils.interruptIgnoredSleep(TimeUtils.secondsToMillis(10));
                        doUpdateObjects(batchUpdateUrl, batchUpdateRequest, create, retries + 1);
                        return;
                    } else {
                        throw new BatchObjectException(new BatchObjectError(null, "Hubspot server unavailable", HubspotErrorCategory.SERVER_DOWN.name()));
                    }
                }
                String errorString = response.readEntity(String.class);
                log.error("Hubspot error:" + errorString);
                log.error("Hubspot error code:" + response.getStatus());
                BatchObjectError batchObjectError = JsonUtils.jsonStringToObject(errorString, BatchObjectError.class);
                if (batchObjectError.getMessage().contains("You have reached your ten_secondly_rolling limit")) {
                    ThreadUtils.interruptIgnoredSleep(TimeUtils.secondsToMillis(5));
                    doUpdateObjects(batchUpdateUrl, batchUpdateRequest, create, retries + 1);
                    return;
                }
                throw new BatchObjectException(batchObjectError);
            }
        }

    }

    public void updateObjects(String batchUpdateUrl, BatchUpdateRequest batchUpdateRequest, boolean create) throws BatchObjectException {
        doUpdateObjects(batchUpdateUrl, batchUpdateRequest, create, 0);

    }

    public void consumeObjects(List<String> properties, String objectType, Consumer<HubspotObject> objectConsumer) {
        MutableObject<PaginatedObjects> paginatedObjects = new MutableObject<>();
        while (paginatedObjects.getValue() == null || paginatedObjects.getValue().hasMore()) {
            paginatedObjects.setValue(executeRequest(() -> {

                WebTarget webTarget = this.client.target(String.format("%s/crm/v3/objects/%s", HUBSPOT_BASE_URL, objectType));
                for (String property : properties) {
                    webTarget = webTarget.queryParam("properties", property);
                }
                if (paginatedObjects.getValue() != null) {
                    webTarget = webTarget.queryParam("after", paginatedObjects.getValue().getPaging().getNext().getAfter());
                }
                webTarget = webTarget.queryParam("limit", 100);
                return webTarget.request(MediaType.APPLICATION_JSON)
                        .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + hubspotAccessConfig.getAccessToken())
                        .get(PaginatedObjects.class);
            }));
            paginatedObjects.getValue().getResults().forEach(objectConsumer);
        }
    }


    private <T> T executeRequest(ThrowingSupplier<T> supplier) {
        try {

            return RetryUtils.retrySupplier(supplier, 3, Lists.newArrayList(NotAuthorizedException.class,
                            SocketTimeoutException.class),
                    ((throwable, attempts) -> {
                        if (throwable instanceof NotAuthorizedException && attempts <= 1) {
                            this.hubspotAccessConfig = accessTokenRefresher.refreshAccessConfig(oauthToken);
                            return new WaitTimeAndRetry(0, true);
                        }
                        if (throwable instanceof SocketTimeoutException && attempts <= 3) {
                            return new WaitTimeAndRetry(TimeUtils.secondsToMillis(10), true);
                        }
                        return new WaitTimeAndRetry(0, false);
                    }));

        } catch (Exception e) {
            throw new CastledRuntimeException(e);
        }
    }
}
