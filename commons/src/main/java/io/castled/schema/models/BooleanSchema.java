package io.castled.schema.models;

import com.google.common.collect.Lists;
import lombok.Builder;

import java.util.Map;

public class BooleanSchema extends Schema {

    @Builder
    public BooleanSchema(boolean optional) {
        super(SchemaType.BOOLEAN, Lists.newArrayList(Boolean.class), optional);
    }
}
