package io.castled.apps.connectors.sendgrid;

import io.castled.exceptions.CastledRuntimeException;
import lombok.Getter;

import java.util.Arrays;

public enum SendgridObject {
    CONTACT("Contact");

    @Getter
    private final String name;

    SendgridObject(String name) {
        this.name = name;
    }

    public static SendgridObject getObjectByName(String name) {
        return Arrays.stream(SendgridObject.values()).filter(sendgridObject -> sendgridObject.getName().equals(name))
                .findFirst().orElseThrow(() -> new CastledRuntimeException(String.format("Invalid object name %s", name)));
    }
}
