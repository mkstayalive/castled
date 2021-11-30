package io.castled.schema.models;

import com.google.common.collect.Lists;
import lombok.Builder;

public class ShortSchema extends Schema {

    @Builder
    public ShortSchema(boolean optional) {
        super(SchemaType.SHORT, Lists.newArrayList(Short.class), optional);
    }
}
