package io.castled.apps.connectors.hubspot.client.dtos;

import lombok.Data;

import java.util.Map;

@Data
public class HubspotObject {

    private String id;
    private Map<String, Object> properties;
}
