package io.castled.apps.connectors.marketo;

import io.castled.apps.connectors.sendgrid.SendgridObject;
import io.castled.exceptions.CastledRuntimeException;
import lombok.Getter;

import java.util.Arrays;

public enum MarketoObject {

    LEADS("leads"),
    COMPANIES("companies"),
    OPPORTUNITIES("opportunities");

    @Getter
    private final String name;

    MarketoObject(String name) {
        this.name = name;
    }

    public static MarketoObject getObjectByName(String name) {
        return Arrays.stream(MarketoObject.values()).filter(marketoObject -> marketoObject.getName().equals(name))
                .findFirst().orElseThrow(() -> new CastledRuntimeException(String.format("Invalid object name %s", name)));
    }
}
