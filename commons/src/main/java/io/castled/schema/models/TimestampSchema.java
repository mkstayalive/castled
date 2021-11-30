package io.castled.schema.models;

import com.google.common.collect.Lists;
import lombok.Builder;

import java.time.LocalDateTime;

public class TimestampSchema extends Schema {

    @Builder
    public TimestampSchema(boolean optional) {
        super(SchemaType.TIMESTAMP, Lists.newArrayList(LocalDateTime.class), optional);
    }
}
