package io.castled.apps.connectors.hubspot.objecthelpers;

import com.google.common.collect.Lists;
import io.castled.apps.connectors.hubspot.client.dtos.HubspotProperty;
import io.castled.apps.connectors.hubspot.client.dtos.HubspotType;

import java.util.List;
import java.util.stream.Collectors;

public class DefaultObjectHelper implements HubspotObjectHelper {

    public List<String> dedupKeyEligibles(List<HubspotProperty> allProperties) {
        return allProperties.stream().filter(hubspotProperty -> !Lists.newArrayList(HubspotType.DATETIME).contains(hubspotProperty.getType()))
                .map(HubspotProperty::getName).collect(Collectors.toList());

    }
}
