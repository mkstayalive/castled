package io.castled.apps.connectors.marketo;

import lombok.Getter;

public enum MarketoSyncMode {

    INSERT("createOnly"),
    UPDATE("updateOnly"),
    UPSERT("createOrUpdate");

    @Getter
    private final String name;

    MarketoSyncMode(String name) { this.name = name; }
}
