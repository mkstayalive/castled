package io.castled.schema.models;

import com.google.common.collect.Lists;
import lombok.Builder;

import java.nio.ByteBuffer;
import java.util.Map;

public class BytesSchema extends Schema {

    @Builder
    public BytesSchema(boolean optional) {
        super(SchemaType.BYTES, Lists.newArrayList(byte[].class, ByteBuffer.class), optional);

    }
}
