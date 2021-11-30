package io.castled.apps.connectors.intercom;

import io.castled.exceptions.CastledRuntimeException;
import lombok.Getter;

import java.util.Arrays;

public enum IntercomObject {
    LEAD("Lead"),
    USER("User"),
    CONTACT("Contact"),
    COMPANY("Company");

    @Getter
    private final String name;

    IntercomObject(String name) {
        this.name = name;
    }

    public static IntercomObject getObjectByName(String name) {
        return Arrays.stream(IntercomObject.values()).filter(intercomObject -> intercomObject.getName().equals(name))
                .findFirst().orElseThrow(() -> new CastledRuntimeException(String.format("Invalid object name %s", name)));
    }

}
