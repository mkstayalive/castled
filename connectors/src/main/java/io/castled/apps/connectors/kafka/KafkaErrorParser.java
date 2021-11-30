package io.castled.apps.connectors.kafka;

import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.errorclassifications.UnclassifiedError;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class KafkaErrorParser {

    public CastledError parseException(Exception e) {
        return new UnclassifiedError(Optional.ofNullable(e.getMessage()).orElse("Unknown Error"));
    }
}
