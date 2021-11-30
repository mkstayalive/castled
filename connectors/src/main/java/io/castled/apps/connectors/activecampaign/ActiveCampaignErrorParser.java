package io.castled.apps.connectors.activecampaign;


import com.google.inject.Singleton;
import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.errorclassifications.MissingRequiredFieldsError;
import io.castled.commons.errors.errorclassifications.UnclassifiedError;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class ActiveCampaignErrorParser {

    public CastledError getPipelineError(String failureReason) {
        return new UnclassifiedError(failureReason);
    }

    public CastledError getMissingRequiredFieldError(String missingField) {
        return new MissingRequiredFieldsError(Stream.of(missingField).collect(Collectors.toList()));
    }
}
