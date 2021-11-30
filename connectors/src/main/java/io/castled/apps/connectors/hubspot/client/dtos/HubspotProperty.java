package io.castled.apps.connectors.hubspot.client.dtos;

import lombok.Data;

@Data
public class HubspotProperty {

    private String name;
    private String label;
    private HubspotType type;
    private boolean calculated;
    private boolean readOnlyValue;
}
