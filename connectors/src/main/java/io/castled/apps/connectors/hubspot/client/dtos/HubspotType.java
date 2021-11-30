package io.castled.apps.connectors.hubspot.client.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

public enum HubspotType {
    STRING("string"),
    NUMBER("number"),
    DATE("date"),
    DATETIME("datetime"),
    ENUMERATION("enumeration"),
    BOOLEAN("bool"),
    PHONE_NUMBER("phone_number");

    @Getter
    private final String displayName;

    @JsonValue
    public String toJsonValue() {
        return displayName;
    }

    @JsonCreator
    public static HubspotType fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(v -> v.displayName.equals(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.valueOf(displayName)));
    }

    HubspotType(String displayName) {
        this.displayName = displayName;
    }
}
