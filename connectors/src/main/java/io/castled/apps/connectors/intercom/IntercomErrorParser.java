package io.castled.apps.connectors.intercom;

import io.castled.apps.connectors.intercom.client.dtos.IntercomErrorResponse;
import io.castled.apps.connectors.intercom.client.dtos.IntercomObjectError;
import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.errorclassifications.ExternallyCategorizedError;

public class IntercomErrorParser {

    public CastledError parseIntercomError(IntercomErrorResponse intercomErrorResponse) {
        IntercomObjectError intercomObjectError = intercomErrorResponse.getErrors().get(0);
        return new ExternallyCategorizedError(intercomObjectError.getCode(), intercomObjectError.getMessage());
    }
}

