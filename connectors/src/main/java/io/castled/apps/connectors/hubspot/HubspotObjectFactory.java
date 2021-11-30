package io.castled.apps.connectors.hubspot;

import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.apps.connectors.hubspot.objecthelpers.CompanyObjectHelper;
import io.castled.apps.connectors.hubspot.objecthelpers.ContactObjectHelper;
import io.castled.apps.connectors.hubspot.objecthelpers.DefaultObjectHelper;
import io.castled.apps.connectors.hubspot.objecthelpers.HubspotObjectHelper;

@Singleton
public class HubspotObjectFactory {

    public HubspotObjectHelper getObjectHelper(String object) {
        if (object.toUpperCase().equals(HubspotStandardObject.CONTACT.name())) {
            return ObjectRegistry.getInstance(ContactObjectHelper.class);
        }
        if (object.toUpperCase().equals(HubspotStandardObject.COMPANY.name())) {
            return ObjectRegistry.getInstance(CompanyObjectHelper.class);
        }
        return ObjectRegistry.getInstance(DefaultObjectHelper.class);
    }
}
