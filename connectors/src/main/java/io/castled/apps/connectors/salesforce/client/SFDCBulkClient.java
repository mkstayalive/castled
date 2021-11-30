package io.castled.apps.connectors.salesforce.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sforce.async.*;
import com.sforce.ws.ConnectorConfig;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.salesforce.SalesforceAccessConfig;
import io.castled.apps.connectors.salesforce.client.dtos.PkChunking;
import io.castled.apps.connectors.salesforce.oauth.SalesforceAccessTokenRefresher;
import io.castled.core.WaitTimeAndRetry;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.functionalinterfaces.ThrowingConsumer;
import io.castled.functionalinterfaces.ThrowingSupplier;
import io.castled.oauth.OAuthClientConfig;
import io.castled.oauth.OAuthDAO;
import io.castled.oauth.OAuthDetails;
import io.castled.utils.JsonStreamParser;
import io.castled.utils.RetryUtils;
import io.castled.utils.ThreadUtils;
import io.castled.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;


@Slf4j
public class SFDCBulkClient {

    private final BulkConnection bulkConnection;
    private final OAuthDetails oAuthDetails;
    private final SalesforceAccessTokenRefresher accessTokenRefresher;

    public SFDCBulkClient(Long oauthToken, OAuthClientConfig oAuthClientConfig) throws Exception {
        OAuthDetails oAuthDetails = ObjectRegistry.getInstance(Jdbi.class)
                .onDemand(OAuthDAO.class).getOAuthDetails(oauthToken);
        this.bulkConnection = new BulkConnection(getConnectorConfig(oAuthDetails));
        this.accessTokenRefresher = new SalesforceAccessTokenRefresher(oAuthClientConfig);
        this.oAuthDetails = oAuthDetails;
    }

    public void runBulkQuery(String query, PkChunking pkChunking, String object,
                             long timeoutMs, ThrowingConsumer<Map<String, Object>> recordConsumer) {
        try {
            JobInfo jobInfo = createBulkJob(constructJobInfo(object, pkChunking));
            createBatchFromStream(jobInfo, new ByteArrayInputStream(query.getBytes()));
            long startTime = System.currentTimeMillis();
            Set<String> completedBatches = Sets.newHashSet();
            JsonStreamParser jsonStreamParser = ObjectRegistry.getInstance(JsonStreamParser.class);

            ThreadUtils.interruptIgnoredSleep(TimeUtils.secondsToMillis(10));
            ;
            while (true) {
                BatchInfoList batchInfoList = getBatchInfoList(jobInfo.getId(), ContentType.JSON);
                for (BatchInfo batchInfo : batchInfoList.getBatchInfo()) {
                    switch (batchInfo.getState()) {
                        case NotProcessed:
                            completedBatches.add(batchInfo.getId());
                            break;
                        case InProgress:
                        case Failed:
                            if (Optional.ofNullable(batchInfo.getStateMessage()).filter(message -> message.contains("QUERY_TIMEOUT")).isPresent()) {
                                throw new AsyncApiException("Query timed out", AsyncExceptionCode.Timeout);
                            }
                            break;
                        case Completed:
                            QueryResultList queryResultList = getQueryResultList(jobInfo.getId(), batchInfo.getId(), ContentType.JSON);
                            for (String result : queryResultList.getResult()) {
                                jsonStreamParser.parseJsonStream(getQueryResultStream(jobInfo.getId(), batchInfo.getId(), result),
                                        recordConsumer);
                            }
                            completedBatches.add(batchInfo.getId());
                            break;
                    }
                }
                if (completedBatches.size() >= batchInfoList.getBatchInfo().length) {
                    closeJob(jobInfo.getId());
                    break;
                }
                if (System.currentTimeMillis() - startTime > timeoutMs) {
                    throw new TimeoutException();
                }
                ThreadUtils.interruptIgnoredSleep(TimeUtils.secondsToMillis(30));
            }

        } catch (Exception e) {
            log.error("Bulk query run failed for query {} and object {}", query, object, e);
            throw new CastledRuntimeException(e);
        }
    }

    private JobInfo constructJobInfo(String object, PkChunking pkChunking) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setObject(object);
        jobInfo.setContentType(ContentType.JSON);
        if (pkChunking.isEnabled()) {
            bulkConnection.addHeader("Sforce-Enable-PKChunking", "chunkSize=" + pkChunking.getChunkSize());
            jobInfo.setOperation(OperationEnum.query);
        } else {
            jobInfo.setOperation(OperationEnum.queryAll);
        }
        return jobInfo;

    }

    private ConnectorConfig getConnectorConfig(OAuthDetails oAuthDetails) {
        ConnectorConfig connectorConfig = new ConnectorConfig();
        connectorConfig.setSessionId(oAuthDetails.getAccessConfig().getAccessToken());
        connectorConfig.setCompression(true);
        connectorConfig.setRestEndpoint(SFDCUtils.getBulkApiEndPoint((SalesforceAccessConfig) oAuthDetails.getAccessConfig()));
        return connectorConfig;
    }

    private JobInfo createBulkJob(JobInfo jobInfo) throws Exception {
        return executeRequest(() -> bulkConnection.createJob(jobInfo, ContentType.JSON));
    }

    private BatchInfo createBatchFromStream(JobInfo jobInfo, InputStream inputStream) throws Exception {
        return executeRequest(() -> bulkConnection.createBatchFromStream(jobInfo, inputStream));
    }

    private BatchInfoList getBatchInfoList(String jobId, ContentType contentType) throws Exception {
        return executeRequest(() -> bulkConnection.getBatchInfoList(jobId, contentType));
    }

    private QueryResultList getQueryResultList(String jobId, String batchId, ContentType contentType) throws Exception {
        return executeRequest(() -> bulkConnection.getQueryResultList(jobId, batchId, contentType));
    }

    private InputStream getQueryResultStream(String jobId, String batchId, String resultId) throws Exception {
        return executeRequest(() -> bulkConnection.getQueryResultStream(jobId, batchId, resultId));
    }

    private void closeJob(String jobId) throws Exception {
        executeRequest(() -> bulkConnection.closeJob(jobId));
    }


    private <T> T executeRequest(ThrowingSupplier<T> supplier) throws Exception {
        return RetryUtils.retrySupplier(supplier, 1, Lists.newArrayList(AsyncApiException.class),
                ((throwable, attempts) -> {
                    AsyncApiException asyncApiException = (AsyncApiException) throwable;
                    if (asyncApiException.getExceptionCode() == AsyncExceptionCode.InvalidSessionId) {
                        this.bulkConnection.getConfig().setSessionId(this.accessTokenRefresher.refreshAccessConfig(oAuthDetails.getId()).getAccessToken());
                        return new WaitTimeAndRetry(0, true);
                    }
                    return new WaitTimeAndRetry(0, false);
                }));

    }

}
