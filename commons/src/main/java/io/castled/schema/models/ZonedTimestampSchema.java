package io.castled.schema.models;

import com.google.common.collect.Lists;
import lombok.Builder;

import java.time.ZonedDateTime;

public class ZonedTimestampSchema extends Schema {

    @Builder
    public ZonedTimestampSchema(boolean optional) {
        super(SchemaType.ZONED_TIMESTAMP, Lists.newArrayList(ZonedDateTime.class), optional);
    }
}
