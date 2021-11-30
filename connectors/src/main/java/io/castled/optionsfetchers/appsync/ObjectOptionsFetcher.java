package io.castled.optionsfetchers.appsync;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.apps.ExternalApp;
import io.castled.apps.ExternalAppConnector;
import io.castled.apps.ExternalAppType;
import io.castled.apps.dtos.AppSyncConfigDTO;
import io.castled.forms.dtos.FormFieldOption;

import java.util.List;
import java.util.Map;

@Singleton
@SuppressWarnings({"unchecked", "rawtypes"})
public class ObjectOptionsFetcher implements AppSyncOptionsFetcher {

    private final Map<ExternalAppType, ExternalAppConnector> externalAppConnectors;

    @Inject
    public ObjectOptionsFetcher(Map<ExternalAppType, ExternalAppConnector> externalAppConnectors) {
        this.externalAppConnectors = externalAppConnectors;
    }

    @Override
    public List<FormFieldOption> getOptions(AppSyncConfigDTO appSyncConfig, ExternalApp externalApp) {
        return this.externalAppConnectors.get(externalApp.getType()).getAllObjects(externalApp.getConfig(),
                appSyncConfig.getAppSyncConfig());
    }
}
