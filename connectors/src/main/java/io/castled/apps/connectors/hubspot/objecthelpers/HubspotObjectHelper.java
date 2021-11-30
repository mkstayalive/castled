package io.castled.apps.connectors.hubspot.objecthelpers;

import io.castled.apps.connectors.hubspot.client.dtos.HubspotProperty;

import java.util.List;
import java.util.stream.Collectors;

public interface HubspotObjectHelper {

    default List<String> dedupKeyEligibles(List<HubspotProperty> allProperties) {
        return allProperties.stream().map(HubspotProperty::getName).collect(Collectors.toList());
    }
}
