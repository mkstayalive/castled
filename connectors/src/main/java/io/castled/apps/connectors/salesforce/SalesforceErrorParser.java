package io.castled.apps.connectors.salesforce;

import com.google.inject.Singleton;
import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.errorclassifications.*;


@Singleton
public class SalesforceErrorParser {

    public CastledError parseSalesforceError(String sfError) {
        if (sfError.contains("DUPLICATE_EXTERNAL_ID")) {
            return new ExistingDuplicatePrimaryKeysError(sfError);
        }
        if (sfError.contains("DUPLICATE_VALUE") && sfError.contains("Duplicate external id specified")) {
            return new DuplicatePrimaryKeyValueError(sfError);
        }
        if (sfError.contains("DUPLICATE_VALUE") && sfError.contains("duplicate value found")) {
            return new DuplicateUniqueKeyValueError(sfError);
        }
        if (sfError.contains("STORAGE_LIMIT_EXCEEDED")) {
            return new StorageLimitExceededError();
        }
        return new UnclassifiedError(sfError);
    }
}
