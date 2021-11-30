package io.castled.schema.models;

import com.google.common.collect.Lists;
import lombok.Builder;

public class IntegerSchema extends Schema {

    @Builder
    public IntegerSchema(boolean optional) {
        super(SchemaType.INT, Lists.newArrayList(Integer.class), optional);
    }
}
