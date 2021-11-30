package io.castled.schema.models;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;

public class StringSchema extends Schema {

    @Getter
    private final int maxLength;

    @Builder
    public StringSchema(boolean optional, int maxLength) {
        super(SchemaType.STRING, Lists.newArrayList(String.class), optional);
        this.maxLength = maxLength;

    }
}
