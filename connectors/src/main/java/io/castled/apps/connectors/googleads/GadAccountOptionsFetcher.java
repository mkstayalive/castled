package io.castled.apps.connectors.googleads;

import com.google.inject.Singleton;
import io.castled.apps.ExternalApp;
import io.castled.apps.dtos.AppSyncConfigDTO;
import io.castled.forms.dtos.FormFieldOption;
import io.castled.optionsfetchers.appsync.AppSyncOptionsFetcher;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class GadAccountOptionsFetcher implements AppSyncOptionsFetcher {

    @Override
    public List<FormFieldOption> getOptions(AppSyncConfigDTO appSyncConfig, ExternalApp externalApp) {
        return GoogleAdUtils.getAllAccessibleCustomers((GoogleAdsAppConfig) externalApp.getConfig())
                .stream().map(customerId -> new FormFieldOption(customerId, customerId)).collect(Collectors.toList());
    }
}
