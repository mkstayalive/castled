package io.castled.apps.connectors.customerio;

import io.castled.exceptions.CastledRuntimeException;
import lombok.Getter;

import java.util.Arrays;

public enum CustomerIOObject {

    EVENT("Event"),
    PERSON("Person");

    @Getter
    private final String name;

    CustomerIOObject(String name) {
        this.name = name;
    }

    public static CustomerIOObject getObjectByName(String name) {
        return Arrays.stream(CustomerIOObject.values()).filter(intercomObject -> intercomObject.getName().equals(name))
                .findFirst().orElseThrow(() -> new CastledRuntimeException(String.format("Invalid object name %s", name)));
    }

}
