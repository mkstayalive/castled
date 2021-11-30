package io.castled.schema.models;

import com.google.common.collect.Lists;
import lombok.Builder;

import java.time.LocalDate;

public class DateSchema extends Schema {

    @Builder
    public DateSchema(boolean optional) {
        super(SchemaType.DATE, Lists.newArrayList(LocalDate.class), optional);
    }
}
