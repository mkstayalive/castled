package io.castled.apps.connectors.intercom.client.models;

import lombok.Getter;

public enum IntercomModel {
    CONTACT("contact"),
    COMPANY("company");

    @Getter
    private final String name;

    IntercomModel(String name) {
        this.name = name;
    }
}
