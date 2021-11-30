package io.castled.commons.errors;

import io.castled.schema.models.Tuple;

public interface CastledErrorTracker {

    void writeError(Tuple record, CastledError pipelineError) throws Exception;

    void flushErrors() throws Exception;
}
