package io.castled.apps.connectors.activecampaign;

import io.castled.exceptions.CastledRuntimeException;
import lombok.Getter;

import java.util.Arrays;

public enum ActiveCampaignObject {

    CONTACT("Contact");

    @Getter
    private final String name;

    ActiveCampaignObject(String name) {
        this.name = name;
    }

    public static ActiveCampaignObject getObjectByName(String name) {
        return Arrays.stream(ActiveCampaignObject.values()).filter(intercomObject -> intercomObject.getName().equals(name))
                .findFirst().orElseThrow(() -> new CastledRuntimeException(String.format("Invalid object name %s", name)));
    }

}
