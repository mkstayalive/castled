package io.castled.schema.models;

import com.google.common.collect.Lists;
import lombok.Builder;

public class DoubleSchema extends Schema {

    @Builder
    public DoubleSchema(boolean optional) {
        super(SchemaType.DOUBLE, Lists.newArrayList(Double.class), optional);
    }
}
