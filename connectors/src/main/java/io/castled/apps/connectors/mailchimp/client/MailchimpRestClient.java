package io.castled.apps.connectors.mailchimp.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.mailchimp.MailchimpAccessConfig;
import io.castled.apps.connectors.mailchimp.client.dtos.*;
import io.castled.apps.connectors.mailchimp.client.models.MailchimpOperationError;
import io.castled.apps.connectors.mailchimp.client.models.MemberAndError;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.oauth.OAuthDAO;
import io.castled.utils.JsonUtils;
import io.castled.utils.RestUtils;
import io.castled.utils.ThreadUtils;
import io.castled.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MailchimpRestClient {

    private static final String API_VERSION = "3.0";
    private final MailchimpAccessConfig mailchimpAccessConfig;
    private final Client client;

    public MailchimpRestClient(Long oauthToken) {
        this.mailchimpAccessConfig = (MailchimpAccessConfig) ObjectRegistry.getInstance(Jdbi.class)
                .onDemand(OAuthDAO.class).getOAuthDetails(oauthToken).getAccessConfig();

        this.client = ObjectRegistry.getInstance(Client.class);
    }

    public static String getErrorResponse(InputStream responseStream) throws IOException {
        try (TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream
                (new GzipCompressorInputStream(responseStream))) {
            ArchiveEntry archiveEntry = null;
            while ((archiveEntry = tarArchiveInputStream.getNextTarEntry()) != null) {
                if (archiveEntry.getName().contains(".json")) {
                    if (tarArchiveInputStream.canReadEntryData(archiveEntry)) {
                        return IOUtils.toString(tarArchiveInputStream, StandardCharsets.UTF_8);
                    }
                }
            }
            return null;
        }
    }

    public List<Audience> getAllAudiences() {
        return this.client.target(String.format("%s/%s/lists", mailchimpAccessConfig.getApiEndPoint(), API_VERSION))
                .request(MediaType.APPLICATION_JSON).header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + this.mailchimpAccessConfig.getAccessToken())
                .get(AudienceListResponse.class).getLists();
    }

    //Returns the list of failed records;
    public List<MemberAndError> upsertMembers(String audienceId, List<MailchimpMember> members) {
        try {
            BatchOperationsResponse batchOperationsResponse = createAudience(audienceId, members);
            ThreadUtils.interruptIgnoredSleep(TimeUtils.secondsToMillis(5));
            while (batchOperationsResponse.getStatus() != BatchOperationStatus.finished) {
                batchOperationsResponse = getBatchStatus(batchOperationsResponse.getId());
                ThreadUtils.interruptIgnoredSleep(TimeUtils.secondsToMillis(2));
            }
            String errorJson = getErrorResponse(this.client.target(batchOperationsResponse.getResponseBodyUrl())
                    .request(MediaType.APPLICATION_OCTET_STREAM)
                    .get(InputStream.class));

            List<MailchimpOperationError> operationErrors = JsonUtils.jsonStringToTypeReference(errorJson, new TypeReference<List<MailchimpOperationError>>() {
            });
            return operationErrors.stream().filter(operationError -> operationError.getStatusCode() > 299)
                    .map(operationError -> new MemberAndError(members.get(Integer.parseInt(operationError.getOperationId())), operationError)).collect(Collectors.toList());
        } catch (IOException e) {
            log.error(String.format("Upsert audience members failed for audience %s", audienceId), e);
            throw new CastledRuntimeException(e);
        }
    }

    private BatchOperationsResponse getBatchStatus(String batchId) {
        return this.client.target(String.format("%s/%s/batches/%s", mailchimpAccessConfig.getApiEndPoint(), API_VERSION, batchId))
                .request(MediaType.APPLICATION_JSON).header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + this.mailchimpAccessConfig.getAccessToken())
                .get(BatchOperationsResponse.class);
    }

    private BatchOperationsResponse createAudience(String audienceId, List<MailchimpMember> members) {
        List<BatchOperation> batchOperations = Lists.newArrayList();
        long operationId = 0;
        for (MailchimpMember mailchimpMember : members) {
            batchOperations.add(buildAudienceOperation(audienceId, operationId, mailchimpMember));
            operationId++;
        }
        return this.client.target(String.format("%s/%s/batches",
                        mailchimpAccessConfig.getApiEndPoint(), API_VERSION))
                .request(MediaType.APPLICATION_JSON).header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + this.mailchimpAccessConfig.getAccessToken())
                .post(Entity.json(new BatchOperationsRequest(batchOperations)), BatchOperationsResponse.class);
    }

    private BatchOperation buildAudienceOperation(String audienceId, long operationId, MailchimpMember mailchimpMember) {
        String subscriberHash = DigestUtils.md5Hex(mailchimpMember.getEmailAddress()).toUpperCase();
        String path = String.format("/lists/%s/members/%s", audienceId, subscriberHash);
        String audienceBody = JsonUtils.objectToString(mailchimpMember);
        return BatchOperation.builder()
                .operationId(String.valueOf(operationId)).method("PUT")
                .body(audienceBody).path(path).build();
    }

}
