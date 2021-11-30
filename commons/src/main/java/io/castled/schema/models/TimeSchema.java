package io.castled.schema.models;

import com.google.common.collect.Lists;
import lombok.Builder;

import java.time.LocalTime;

public class TimeSchema extends Schema {

    @Builder
    public TimeSchema(boolean optional) {
        super(SchemaType.TIME, Lists.newArrayList(LocalTime.class), optional);

    }
}
