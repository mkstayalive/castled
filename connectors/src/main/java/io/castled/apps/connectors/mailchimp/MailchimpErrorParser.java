package io.castled.apps.connectors.mailchimp;


import com.google.inject.Singleton;
import io.castled.apps.connectors.mailchimp.client.models.MailchimpOperationError;
import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.errorclassifications.UnclassifiedError;

@Singleton
public class MailchimpErrorParser {

    public CastledError getPipelineError(MailchimpOperationError operationError) {
        return new UnclassifiedError(operationError.getResponse());
    }
}
