package io.castled.apps.connectors.marketo;

import com.google.api.client.util.Lists;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.marketo.dtos.*;
import io.castled.apps.connectors.marketo.exception.TokenExpiredException;
import io.castled.core.WaitTimeAndRetry;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.functionalinterfaces.ThrowingConsumer;
import io.castled.functionalinterfaces.ThrowingSupplier;
import io.castled.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.media.multipart.*;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class MarketoBulkClient {

    private final MarketoAppConfig appConfig;
    private final MarketoAuthClient authClient;
    private final Client client;

    private static final String OBJECT_NOT_FOUND = "1013";
    private static final String OBJECT_FOUND = "1017";
    private static final String ERROR_NOT_FOUND = "1004";
    private static final String ERROR_FOUND = "1005";
    private static final String[] WHITE_LIST_ERRORS = {
            OBJECT_NOT_FOUND,
            OBJECT_FOUND,
            ERROR_NOT_FOUND,
            ERROR_FOUND
    };

    public MarketoBulkClient(MarketoAppConfig appConfig) {
        this.appConfig = appConfig;
        authClient = new MarketoAuthClient(appConfig);
        client = ObjectRegistry.getInstance(Client.class);
    }

    public ObjectAttributesContainer getAttributes(MarketoObject object) {
        final String url = String.format("%s/rest/v1/%s/describe.json", appConfig.getBaseUrl(), object.getName());
        List<GenericAttribute> result;
        ObjectAttributesContainer attrsContainer;
        if (object == MarketoObject.LEADS) {
            LeadAttributesResponse response = this.client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + authClient.getToken())
                    .get(LeadAttributesResponse.class);
            result = response.getResult();
            List<String> primaryKeys = response.getResult().stream().map(attrRef -> attrRef.getRest().getName())
                    .filter(fieldName -> !fieldName.equals(LeadAttribute.ID)).collect(Collectors.toList());
            attrsContainer = new ObjectAttributesContainer(result, primaryKeys, LeadAttribute.ID, object);
        } else {
            GenericAttributesResponse response = this.client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + authClient.getToken())
                    .get(GenericAttributesResponse.class);
            GenericAttributesWrapper wrapper = response.getResult().stream().findFirst().get();
            attrsContainer = new ObjectAttributesContainer(wrapper.getFields(), wrapper.getDedupeFields(), wrapper.getIdField(), object);
        }
        return attrsContainer;
    }

    private Predicate<BatchSyncResult> isRecordSyncFailed = (BatchSyncResult result) -> {
        // Marketo return status as skipped for certain errors that can be user corrected.
        // So all skipped except error code 1004 and 1005 reported as failure to user.
        return BatchSyncResult.FAILED.equals(result.getStatus()) ||
                (BatchSyncResult.SKIPPED.equals(result.getStatus()) &&
                        Arrays.stream(WHITE_LIST_ERRORS)
                                .noneMatch(error -> error.equals(result.getReasons().stream().findFirst().get().getCode())));
    };

    private Predicate<BatchSyncResult> isRecordSyncSkipped = (BatchSyncResult result) -> {
        // Marketo return status as skipped for certain errors that can be user corrected.
        // So all skipped with error code 1004 and 1005 reported as skipped to user.
        return BatchSyncResult.SKIPPED.equals(result.getStatus()) &&
                (Arrays.stream(WHITE_LIST_ERRORS)
                        .anyMatch(error -> error.equals(result.getReasons().stream().findFirst().get().getCode())));
    };

    public BatchSyncStats batchSyncObject(MarketoObject object, GenericObjectSyncRequest request) {
        final String url = String.format("%s/rest/v1/%s.json", appConfig.getBaseUrl(), object.getName());

        ThrowingSupplier<BatchSyncRequestResponse> requestSupplier = () -> {
            BatchSyncRequestResponse response = this.client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + authClient.getToken())
                    .post(Entity.json(request), BatchSyncRequestResponse.class);
            if (!response.getSuccess()) {
                errorConsumer.accept(response.getErrors());
            }
            return response;
        };
        // Batch update
        BatchSyncRequestResponse response = executeRequest(requestSupplier);

        List<MarketoSyncError> marketoErrors = Lists.newArrayList();
        long skippedCount = 0;
        if (!response.getSuccess()) {
            // All records failed
            ErrorResponse err = response.getErrors().stream().findFirst().get();
            IntStream.rangeClosed(0, request.getInput().size())
                    .forEach(i -> marketoErrors.add(new MarketoSyncError(i, err.getCode(), err.getMessage())));
        } else {
            response.getResult().stream()
                    .filter(isRecordSyncFailed)
                    .forEach(resultRef -> marketoErrors.add(new MarketoSyncError(resultRef.getSeq(),
                            resultRef.getReasons().stream().findFirst().get().getCode(),
                            resultRef.getReasons().stream().findFirst().get().getMessage())));
            skippedCount = response.getResult().stream()
                    .filter(isRecordSyncSkipped)
                    .count();
        }
        return new BatchSyncStats(skippedCount, marketoErrors);
    }

    public BatchSyncStats batchUpdateLeads(BatchLeadUpdateRequest request) {
        final String url = String.format("%s/rest/v1/leads.json", appConfig.getBaseUrl());
        ThrowingSupplier<BatchSyncRequestResponse> requestSupplier = () -> {
            BatchSyncRequestResponse response = this.client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + authClient.getToken())
                    .post(Entity.json(request), BatchSyncRequestResponse.class);
            if (!response.getSuccess()) {
                errorConsumer.accept(response.getErrors());
            }
            return response;
        };
        // Batch update
        BatchSyncRequestResponse response = executeRequest(requestSupplier);

        List<MarketoSyncError> marketoErrors = Lists.newArrayList();
        long skippedCount = 0;
        if (!response.getSuccess()) {
            // All records failed
            ErrorResponse err = response.getErrors().stream().findFirst().get();
            IntStream.rangeClosed(0, request.getInput().size())
                    .forEach(i -> marketoErrors.add(new MarketoSyncError(i, err.getCode(), err.getMessage())));
        } else {
            List<BatchSyncResult> results = response.getResult();
            IntStream.range(0, results.size())
                    .filter(i -> isRecordSyncFailed.test(results.get(i)))
                    .forEach(i -> marketoErrors.add(new MarketoSyncError(i,
                            results.get(i).getReasons().stream().findFirst().get().getCode(),
                            results.get(i).getReasons().stream().findFirst().get().getMessage())));

            skippedCount = results.stream().filter(isRecordSyncSkipped).count();
        }
        return new BatchSyncStats(skippedCount, marketoErrors);
    }

    List<MarketoSyncError> bulkUploadLeads(ByteArrayOutputStream leadStream, String primaryKey, Integer msgCount) {
        List<MarketoSyncError> marketoErrors = Lists.newArrayList();

        final MediaType TEXT_CSV_TYPE = new MediaType("text", "csv");
        StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart("file",
                new ByteArrayInputStream(leadStream.toByteArray()), "leads.csv", TEXT_CSV_TYPE);

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        formDataMultiPart.field("format", "csv");
        if (primaryKey != null) {
            formDataMultiPart.field("lookupField", primaryKey);
        }
        formDataMultiPart.bodyPart(streamDataBodyPart);

        final String url = String.format("%s/bulk/v1/leads.json", appConfig.getBaseUrl());

        ThrowingSupplier<BulkSyncRequestResponse> requestSupplier = () -> {
            BulkSyncRequestResponse response = this.client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + authClient.getToken())
                    .post(Entity.entity(formDataMultiPart, Boundary.addBoundary(formDataMultiPart.getMediaType())), BulkSyncRequestResponse.class);

            if (!response.getSuccess()) {
                errorConsumer.accept(response.getErrors());
            }
            return response;
        };
        // Bulk update
        BulkSyncRequestResponse response = executeRequest(requestSupplier);

        // Poll for completion
        BulkSyncRequestStatus status = null;
        do {
            ThreadUtils.interruptIgnoredSleep(TimeUtils.secondsToMillis(5));
            status = getBulkRequestStatus(response);
            System.out.println(JsonUtils.objectToString(status));
        } while (BulkSyncRequestStatus.STATUS_QUEUED.equals(status.getStatus()) ||
                BulkSyncRequestStatus.STATUS_IMPORTING.equals(status.getStatus()));

        if (!response.getSuccess() || BulkSyncRequestStatus.STATUS_FAILED.equals(status.getStatus())) {
            // All records failed
            ErrorResponse err = response.getErrors().stream().findFirst().get();
            IntStream.rangeClosed(0, msgCount)
                    .forEach(i -> marketoErrors.add(new MarketoSyncError(i, err.getCode(), err.getMessage())));
        } else if (status.getNumOfRowsFailed() > 0) {
            // A few failures
            // TODO: Need to handle this
        }
        return marketoErrors;
    }

    private BulkSyncRequestStatus getBulkRequestStatus(BulkSyncRequestResponse res) {
        final BulkResult result = res.getResult().stream().findFirst().get();
        final String url = String.format("%s/bulk/v1/leads/batch/%s.json", appConfig.getBaseUrl(), result.getBatchId());

        ThrowingSupplier<BulkRequestStatusResponse> requestSupplier = () -> {
            BulkRequestStatusResponse response = this.client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .header(RestUtils.AUTHORIZATION_HEADER, "Bearer " + authClient.getToken())
                    .get(BulkRequestStatusResponse.class);

            if (!response.getSuccess()) {
                errorConsumer.accept(response.getErrors());
            }
            return response;
        };
        BulkRequestStatusResponse response = executeRequest(requestSupplier);
        return response.getResult().stream().findFirst().get();
    }

    private ThrowingConsumer<List<ErrorResponse>> errorConsumer = (List<ErrorResponse> errors) -> {
        ErrorResponse err = errors.stream().findFirst().get();
        switch (err.getCode()) {
            case "602":
                throw new TokenExpiredException(err);
        }
    };

    private <T> T executeRequest(ThrowingSupplier<T> supplier) {
        try {
            return RetryUtils.retrySupplier(supplier, 1, Arrays.asList(TokenExpiredException.class),
                    ((throwable, attempts) -> {
                        if (throwable instanceof TokenExpiredException && attempts <= 1) {
                            return new WaitTimeAndRetry(0, true);
                        }
                        return new WaitTimeAndRetry(0, false);
                    }));

        } catch (Exception e) {
            throw new CastledRuntimeException(e);
        }
    }
}