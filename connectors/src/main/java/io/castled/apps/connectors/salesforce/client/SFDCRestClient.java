package io.castled.apps.connectors.salesforce.client;

import com.google.common.collect.Lists;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.salesforce.SalesforceAccessConfig;
import io.castled.apps.connectors.salesforce.client.dtos.*;
import io.castled.apps.connectors.salesforce.oauth.SalesforceAccessTokenRefresher;
import io.castled.core.WaitTimeAndRetry;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.functionalinterfaces.ThrowingSupplier;
import io.castled.oauth.OAuthClientConfig;
import io.castled.oauth.OAuthDAO;
import io.castled.oauth.OAuthDetails;
import io.castled.utils.RestUtils;
import io.castled.utils.RetryUtils;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Slf4j
public class SFDCRestClient {

    private final Client client;
    private final SalesforceAccessTokenRefresher accessTokenRefresher;
    private final Long oauthToken;
    private SalesforceAccessConfig salesforceAccessConfig;

    public SFDCRestClient(Long oauthToken, OAuthClientConfig oAuthClientConfig) {
        this.oauthToken = oauthToken;
        this.client = ObjectRegistry.getInstance(Client.class);
        OAuthDetails oAuthDetails = ObjectRegistry.getInstance(Jdbi.class)
                .onDemand(OAuthDAO.class).getOAuthDetails(oauthToken);
        this.salesforceAccessConfig = (SalesforceAccessConfig) oAuthDetails.getAccessConfig();
        this.accessTokenRefresher = new SalesforceAccessTokenRefresher(oAuthClientConfig);
    }

    public List<SFDCObject> getAllObjects() {
        return executeRequest(() -> this.client.target(String.format("%s/sobjects/", SFDCUtils.getRestEndPoint(salesforceAccessConfig)))
                .request(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + salesforceAccessConfig.getAccessToken())
                .get(SFDCObjectResult.class).getSObjects());

    }

    public SFDCObjectDetails getObjectDetails(String objectName) {
        return executeRequest(() -> this.client.target(String.format("%s/sobjects/%s/describe/", SFDCUtils.getRestEndPoint(salesforceAccessConfig), objectName))
                .request(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + salesforceAccessConfig.getAccessToken())
                .get(SFDCObjectDetails.class));
    }

    public Job createJob(JobRequest jobRequest) {
        return executeRequest(() -> this.client.target(String.format("%s/jobs/ingest", SFDCUtils.getRestEndPoint(salesforceAccessConfig)))
                .request()
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + salesforceAccessConfig.getAccessToken())
                .post(Entity.json(jobRequest), Job.class));

    }

    public void uploadCsv(String jobId, String csvContent) {
        executeRequest(() -> this.client.target(String.format("%s/jobs/ingest/%s/batches",
                        SFDCUtils.getRestEndPoint(salesforceAccessConfig), jobId))
                .request()
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + salesforceAccessConfig.getAccessToken())
                .put(Entity.entity(csvContent, "text/csv")));
    }

    public Job updateJobState(String jobId, JobStateUpdateRequest jobStateUpdateRequest) {
        return executeRequest(() -> this.client.target(String.format("%s/jobs/ingest/%s",
                        SFDCUtils.getRestEndPoint(salesforceAccessConfig), jobId))
                .request(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + salesforceAccessConfig.getAccessToken())
                .method("PATCH", Entity.json(jobStateUpdateRequest), Job.class));

    }

    public Job getJob(String jobId) {
        return executeRequest(() -> this.client.target(String.format("%s/jobs/ingest/%s", SFDCUtils.getRestEndPoint(salesforceAccessConfig), jobId))
                .request(MediaType.APPLICATION_JSON)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + salesforceAccessConfig.getAccessToken())
                .get(Job.class));
    }

    public String getFailedReport(String jobId) {
        return executeRequest(() -> this.client.target(String.format("%s/jobs/ingest/%s/failedResults", SFDCUtils.getRestEndPoint(salesforceAccessConfig), jobId))
                .request(MediaType.TEXT_PLAIN)
                .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + salesforceAccessConfig.getAccessToken())
                .get(String.class));
    }

    private <T> T executeRequest(ThrowingSupplier<T> supplier) {
        try {
            return RetryUtils.retrySupplier(supplier, 1, Lists.newArrayList(NotAuthorizedException.class),
                    ((throwable, attempts) -> {
                        this.salesforceAccessConfig = accessTokenRefresher.refreshAccessConfig(oauthToken);
                        return new WaitTimeAndRetry(0, true);
                    }));
        } catch (Exception e) {
            throw new CastledRuntimeException(e);
        }
    }
}