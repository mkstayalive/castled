package io.castled.apps.connectors.activecampaign.models;

import lombok.Getter;

public enum ActiveCampaignModel {
    CONTACT("contact");

    @Getter
    private final String name;

    ActiveCampaignModel(String name) {
        this.name = name;
    }
}
