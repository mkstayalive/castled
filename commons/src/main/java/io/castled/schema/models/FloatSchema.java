package io.castled.schema.models;

import com.google.common.collect.Lists;
import lombok.Builder;

public class FloatSchema extends Schema {

    @Builder
    public FloatSchema(boolean optional) {
        super(SchemaType.FLOAT, Lists.newArrayList(Float.class), optional);
    }
}
