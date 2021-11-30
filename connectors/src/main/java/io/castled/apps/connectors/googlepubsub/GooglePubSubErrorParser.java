package io.castled.apps.connectors.googlepubsub;

import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.InvalidArgumentException;
import io.castled.ObjectRegistry;
import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.errorclassifications.ExternallyCategorizedError;
import io.castled.commons.errors.errorclassifications.UnclassifiedError;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class GooglePubSubErrorParser {

    public CastledError parseException(Throwable throwable) {
        if (throwable instanceof ApiException) {
            return new ExternallyCategorizedError(((ApiException) throwable)
                    .getStatusCode().getCode().name(), throwable.getCause().getMessage());
        }
        return new UnclassifiedError(Optional.ofNullable(throwable.getMessage()).orElse("Unknown Error"));
    }
}
