package io.castled.schema;

import io.castled.schema.models.*;

public class      SchemaConstants {

    public static final StringSchema STRING_SCHEMA = StringSchema.builder().build();
    public static final StringSchema OPTIONAL_STRING_SCHEMA = StringSchema.builder().optional(true).build();

    public static final EmailSchema EMAIL_SCHEMA = EmailSchema.builder().build();
    public static final EmailSchema OPTIONAL_EMAIL_SCHEMA = EmailSchema.builder().optional(true).build();

    public static final DateSchema DATE_SCHEMA = DateSchema.builder().build();
    public static final DateSchema OPTIONAL_DATE_SCHEMA = DateSchema.builder().optional(true).build();

    public static final TimestampSchema TIMESTAMP_SCHEMA = TimestampSchema.builder().build();
    public static final TimestampSchema OPTIONAL_TIMESTAMP_SCHEMA = TimestampSchema.builder().optional(true).build();

    public static final ZonedTimestampSchema ZONED_TIMESTAMP_SCHEMA = ZonedTimestampSchema.builder().build();
    public static final ZonedTimestampSchema OPTIONAL_ZONED_TIMESTAMP_SCHEMA = ZonedTimestampSchema.builder().optional(true).build();

    public static final LongSchema LONG_SCHEMA = LongSchema.builder().build();
    public static final LongSchema OPTIONAL_LONG_SCHEMA = LongSchema.builder().optional(true).build();

    public static final TimeSchema TIME_SCHEMA = TimeSchema.builder().build();
    public static final TimeSchema OPTIONAL_TIME_SCHEMA = TimeSchema.builder().optional(true).build();

    public static final BooleanSchema BOOL_SCHEMA = BooleanSchema.builder().build();
    public static final BooleanSchema OPTIONAL_BOOL_SCHEMA = BooleanSchema.builder().optional(true).build();

    public static final FloatSchema FLOAT_SCHEMA = FloatSchema.builder().build();
    public static final FloatSchema OPTIONAL_FLOAT_SCHEMA = FloatSchema.builder().optional(true).build();

    public static final DoubleSchema DOUBLE_SCHEMA = DoubleSchema.builder().build();
    public static final DoubleSchema OPTIONAL_DOUBLE_SCHEMA = DoubleSchema.builder().optional(true).build();

    public static final ShortSchema SHORT_SCHEMA = ShortSchema.builder().build();
    public static final ShortSchema OPTIONAL_SHORT_SCHEMA = ShortSchema.builder().optional(true).build();

    public static final BytesSchema BYTES_SCHEMA = BytesSchema.builder().build();
    public static final BytesSchema OPTIONAL_BYTES_SCHEMA = BytesSchema.builder().optional(true).build();
}
