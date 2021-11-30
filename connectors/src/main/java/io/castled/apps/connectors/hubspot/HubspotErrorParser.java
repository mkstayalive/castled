package io.castled.apps.connectors.hubspot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Singleton;
import io.castled.apps.connectors.hubspot.client.HubspotErrorCategory;
import io.castled.apps.connectors.hubspot.client.dtos.BatchObjectError;
import io.castled.apps.connectors.hubspot.client.dtos.RecordError;
import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.errorclassifications.ExternallyCategorizedError;
import io.castled.commons.errors.errorclassifications.InvalidFieldValueError;
import io.castled.commons.errors.errorclassifications.UnclassifiedError;
import io.castled.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class HubspotErrorParser {

    private static final Pattern INVALID_FIELD_VALUE_PATTERN = Pattern.compile("Property values were not valid: (.*)");

    public CastledError parseError(RecordError recordError) {
        return new UnclassifiedError(recordError.getExtraContext());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class InvalidValueError {
        private String message;
        private String error;
        private String name;

    }

    public CastledError parseError(BatchObjectError batchObjectError) {
        if (batchObjectError == null) {
            return new UnclassifiedError("Unknown error");
        }
        if (batchObjectError.getCategory() == null) {
            return new UnclassifiedError(Optional.ofNullable(batchObjectError.getMessage()).orElse("unknown error"));
        }
        if (batchObjectError.getCategory().equals(HubspotErrorCategory.VALIDATION_ERROR.name())) {
            return parseInvalidFieldValueError(batchObjectError);
        }
        return new ExternallyCategorizedError(batchObjectError.getCategory(), batchObjectError.getMessage());
    }

    private CastledError parseInvalidFieldValueError(BatchObjectError batchObjectError) {
        Matcher matcher = INVALID_FIELD_VALUE_PATTERN.matcher(batchObjectError.getMessage());
        if (matcher.find()) {
            List<InvalidValueError> invalidValueErrors = JsonUtils.jsonStringToTypeReference(matcher.group(1), new TypeReference<List<InvalidValueError>>() {
            });
            if (invalidValueErrors.size() == 1) {
                InvalidValueError invalidValueError = invalidValueErrors.get(0);
                return new InvalidFieldValueError(invalidValueError.getName(), invalidValueError.getError(), invalidValueError.getMessage());
            }
        }
        return new ExternallyCategorizedError(batchObjectError.getCategory(), batchObjectError.getMessage());
    }
}
