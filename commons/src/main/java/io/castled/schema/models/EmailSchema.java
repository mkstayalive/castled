package io.castled.schema.models;

import com.google.common.collect.Lists;
import lombok.Builder;

public class EmailSchema extends Schema {

    @Builder
    public EmailSchema(boolean optional) {
        super(SchemaType.EMAIL, Lists.newArrayList(String.class), optional);
    }
}
