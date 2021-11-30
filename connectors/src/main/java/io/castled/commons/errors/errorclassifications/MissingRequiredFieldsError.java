package io.castled.commons.errors.errorclassifications;


import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.CastledErrorCode;

import java.util.Collections;
import java.util.List;

public class MissingRequiredFieldsError extends CastledError {

    private final List<String> missingFields;

    public MissingRequiredFieldsError(List<String> missingFields) {
        super(CastledErrorCode.REQUIRED_FIELDS_MISSING);
        this.missingFields = missingFields;

    }

    @Override
    public String uniqueId() {
        Collections.sort(missingFields);
        return String.join("_", missingFields);
    }

    @Override
    public String description() {
        if (missingFields.size() == 1) {
            return String.format("Required field [%s] missing", missingFields.get(0));
        }
        return String.format("Required fields [%s] missing", String.join(",", missingFields));
    }
}
