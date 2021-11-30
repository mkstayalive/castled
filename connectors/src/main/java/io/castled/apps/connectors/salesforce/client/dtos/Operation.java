package io.castled.apps.connectors.salesforce.client.dtos;

import lombok.Getter;

public enum Operation {
    INSERT("insert"),
    UPSERT("upsert");

    Operation(String name) {
        this.name = name;
    }

    @Getter
    private final String name;
}
