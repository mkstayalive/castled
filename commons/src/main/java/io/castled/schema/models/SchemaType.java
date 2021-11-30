package io.castled.schema.models;

import lombok.Getter;

public enum SchemaType {
    SHORT("Short"),
    INT("Integer"),
    LONG("Long"),
    FLOAT("Float"),
    DOUBLE("Double"),
    BOOLEAN("Boolean"),
    STRING("String"),
    DECIMAL("Decimal"),
    BYTES("Bytes"),
    DATE("Date"),
    TIME("Time"),
    TIMESTAMP("Timestamp"),
    ZONED_TIMESTAMP("Zoned Timestamp"),
    EMAIL("Email");

    @Getter
    private final String displayName;

    SchemaType(String displayName) {
        this.displayName = displayName;
    }
}
