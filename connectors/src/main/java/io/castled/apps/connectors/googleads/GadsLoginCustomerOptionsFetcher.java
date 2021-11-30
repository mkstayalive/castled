package io.castled.apps.connectors.googleads;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.inject.Singleton;
import io.castled.apps.ExternalApp;
import io.castled.apps.dtos.AppSyncConfigDTO;
import io.castled.forms.dtos.FormFieldOption;
import io.castled.optionsfetchers.appsync.AppSyncOptionsFetcher;

import java.util.Collections;
import java.util.List;

@Singleton
public class GadsLoginCustomerOptionsFetcher implements AppSyncOptionsFetcher {
    @Override
    public List<FormFieldOption> getOptions(AppSyncConfigDTO config, ExternalApp externalApp) {
        GoogleAdsAppConfig googleAdsAppConfig = (GoogleAdsAppConfig) externalApp.getConfig();
        GoogleAdsAppSyncConfig googleAdsMappingConfig = (GoogleAdsAppSyncConfig) config.getAppSyncConfig();
        GoogleAdsClient googleAdsClient = GoogleAdUtils.getGoogleAdsClient(googleAdsAppConfig, null);
        String loginCustomerId = GoogleAdUtils.getLoginCustomerId(googleAdsClient, googleAdsMappingConfig.getAccountId());
        return Collections.singletonList(new FormFieldOption(loginCustomerId, loginCustomerId, null));
    }
}
