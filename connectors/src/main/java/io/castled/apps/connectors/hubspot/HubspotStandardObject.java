package io.castled.apps.connectors.hubspot;

import lombok.Getter;

import java.util.Arrays;

public enum HubspotStandardObject {
    CONTACT("Contact", "contacts"),
    COMPANY("Company", "companies"),
    TICKET("Ticket", "tickets"),
    DEAL("Deal", "deals");

    @Getter
    private final String objectName;

    @Getter
    private final String objectUrl;

    HubspotStandardObject(String objectName, String objectUrl) {
        this.objectName = objectName;
        this.objectUrl = objectUrl;
    }


    public static HubspotStandardObject fromName(String name) {
        return Arrays.stream(HubspotStandardObject.values()).filter(hubspotStandardObject -> hubspotStandardObject.getObjectName().equals(name))
                .findFirst().orElse(null);
    }

}
