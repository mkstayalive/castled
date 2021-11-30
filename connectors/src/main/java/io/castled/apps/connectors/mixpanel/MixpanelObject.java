package io.castled.apps.connectors.mixpanel;

import io.castled.exceptions.CastledRuntimeException;
import lombok.Getter;

import java.util.Arrays;

public enum MixpanelObject {

    EVENT("Event"),
    GROUP_PROFILE("Group Profile"),
    USER_PROFILE("User Profile");

    @Getter
    private final String name;

    MixpanelObject(String name) {
        this.name = name;
    }

    public static MixpanelObject getObjectByName(String name) {
        return Arrays.stream(MixpanelObject.values()).filter(intercomObject -> intercomObject.getName().equals(name))
                .findFirst().orElseThrow(() -> new CastledRuntimeException(String.format("Invalid object name %s", name)));
    }

}
