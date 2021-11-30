package io.castled.schema.models;

import com.google.common.collect.Lists;
import lombok.Builder;

public class LongSchema extends Schema {

    @Builder
    public LongSchema(boolean optional) {
        super(SchemaType.LONG, Lists.newArrayList(Long.class), optional);
    }
}
