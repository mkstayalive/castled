package io.castled.apps.connectors.activecampaign.models;

import lombok.Getter;

public enum CustomFieldType {

    TEXTAREA("textarea"),
    TEXT("date"),
    DATE("date"),
    DROPDOWN("dropdown"),
    MULTISELECT("listbox"),
    RADIO("radio"),
    CHECKBOX("datetime"),
    DATETIME("datetime");

    @Getter
    private final String type;

    CustomFieldType(String type) {
        this.type = type;
    }
}
