package io.castled.apps.connectors.hubspot.objecthelpers;

import com.google.common.collect.Lists;
import io.castled.apps.connectors.hubspot.HubspotConsts;
import io.castled.apps.connectors.hubspot.client.dtos.HubspotProperty;

import java.util.List;

public class ContactObjectHelper implements HubspotObjectHelper {

    public List<String> dedupKeyEligibles(List<HubspotProperty> allProperties) {
        return Lists.newArrayList(HubspotConsts.EMAIL_FIELD);
    }
}
