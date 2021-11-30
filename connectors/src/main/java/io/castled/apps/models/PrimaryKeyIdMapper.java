package io.castled.apps.models;

import java.util.List;
import java.util.Map;

public class PrimaryKeyIdMapper<IDTYPE> {

    private final Map<List<Object>, IDTYPE> objectIds;

    public PrimaryKeyIdMapper(Map<List<Object>, IDTYPE> objectIds) {
        this.objectIds = objectIds;
    }

    public IDTYPE getObjectId(List<Object> primaryKeyValues) {
        return objectIds.get(primaryKeyValues);
    }
}
