package io.castled.apps.connectors.customerio;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import io.castled.apps.ExternalApp;
import io.castled.apps.dtos.AppSyncConfigDTO;
import io.castled.forms.dtos.FormFieldOption;
import io.castled.optionsfetchers.appsync.AppSyncOptionsFetcher;

import java.util.List;

@Singleton
public class CIOEventTypeFetcher implements AppSyncOptionsFetcher {

    @Override
    public List<FormFieldOption> getOptions(AppSyncConfigDTO appSyncConfig, ExternalApp externalApp) {
        return Lists.newArrayList(new FormFieldOption("event", "Event"),new FormFieldOption("pageView", "Page View"));
    }
}
