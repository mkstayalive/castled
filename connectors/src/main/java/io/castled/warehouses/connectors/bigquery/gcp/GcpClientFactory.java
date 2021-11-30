package io.castled.warehouses.connectors.bigquery.gcp;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.http.HttpTransportOptions;
import com.google.cloud.storage.StorageOptions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.filestorage.GcsClient;
import io.castled.utils.JsonUtils;
import io.castled.commons.models.ServiceAccountDetails;
import io.castled.warehouses.connectors.bigquery.daos.ServiceAccountDetailsDAO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Singleton
@Slf4j
public class GcpClientFactory {

    private final Cache<CacheKey, BigQuery> bigQueryClientCache;
    private final Cache<CacheKey, GcsClient> gcsClientCache;
    private final ServiceAccountDetailsDAO serviceAccountDetailsDAO;

    @Inject
    public GcpClientFactory(Jdbi jdbi) {
        this.bigQueryClientCache = Caffeine.newBuilder()
                .expireAfterWrite(3, TimeUnit.HOURS)
                .maximumSize(1000).build();

        this.gcsClientCache = Caffeine.newBuilder()
                .expireAfterWrite(3, TimeUnit.HOURS)
                .maximumSize(1000).build();
        this.serviceAccountDetailsDAO = jdbi.onDemand(ServiceAccountDetailsDAO.class);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static final class CacheKey {

        private String clientId;
        private String clientEmail;
        private String clientProjectId;
        private String projectId;
    }

    public BigQuery getBigQuery(String serviceAccount, String projectId) {

        ServiceAccountDetails serviceAccountDetails = serviceAccountDetailsDAO.getServiceAccount(serviceAccount).getServiceAccountDetails();
        CacheKey cacheKey = new CacheKey(serviceAccountDetails.getClientId(), serviceAccountDetails.getClientEmail(),
                serviceAccountDetails.getProjectId(), projectId);
        return this.bigQueryClientCache.get(cacheKey, cacheKeyRef -> {
            Credentials credentials = getCredentials(serviceAccountDetails);
            HttpTransportOptions transportOptions = BigQueryOptions.getDefaultHttpTransportOptions().toBuilder()
                    .setConnectTimeout(60000).setReadTimeout(120000).build();

            return BigQueryOptions.newBuilder().setProjectId(projectId)
                    .setTransportOptions(transportOptions).setCredentials(credentials)
                    .build().getService();
        });
    }


    public GcsClient getGcsClient(String serviceAccount, String projectId) {

        ServiceAccountDetails serviceAccountDetails = serviceAccountDetailsDAO.getServiceAccount(serviceAccount).getServiceAccountDetails();
        CacheKey cacheKey = new CacheKey(serviceAccountDetails.getClientId(), serviceAccountDetails.getClientEmail(),
                serviceAccountDetails.getProjectId(), projectId);
        return this.gcsClientCache.get(cacheKey, cacheKeyRef -> new GcsClient(StorageOptions.newBuilder().setProjectId(projectId)
                .setCredentials(getCredentials(serviceAccountDetails)).build().getService()));
    }

    public Credentials getCredentials(ServiceAccountDetails serviceAccountDetails) {
        try {
            InputStream serviceAccountStream = new ByteArrayInputStream(JsonUtils.objectToByteArray(serviceAccountDetails));
            return GoogleCredentials.fromStream(serviceAccountStream)
                    .createScoped(Collections.singleton("https://www.googleapis.com/auth/cloud-platform"));
        } catch (IOException e) {
            log.error(String.format("Service account credentials fetch failed for %s", serviceAccountDetails.getClientEmail()));
            throw new CastledRuntimeException(e);
        }
    }
}
