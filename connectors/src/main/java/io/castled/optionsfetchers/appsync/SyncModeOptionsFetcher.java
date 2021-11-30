package io.castled.optionsfetchers.appsync;

import com.google.inject.Inject;
import io.castled.apps.ExternalApp;
import io.castled.apps.ExternalAppConnector;
import io.castled.apps.ExternalAppType;
import io.castled.apps.dtos.AppSyncConfigDTO;
import io.castled.commons.models.AppSyncMode;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.forms.dtos.FormFieldOption;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SyncModeOptionsFetcher implements AppSyncOptionsFetcher {

    private final Map<ExternalAppType, ExternalAppConnector> externalAppConnectors;

    @Inject
    public SyncModeOptionsFetcher(Map<ExternalAppType, ExternalAppConnector> externalAppConnectors) {
        this.externalAppConnectors = externalAppConnectors;
    }

    @Override
    public List<FormFieldOption> getOptions(AppSyncConfigDTO appSyncConfig, ExternalApp externalApp) {
        List<AppSyncMode> syncModes = this.externalAppConnectors.get(externalApp.getType()).getSyncModes(externalApp.getConfig(),
                appSyncConfig.getAppSyncConfig());
        return syncModes.stream().map(this::getFormFieldOption).collect(Collectors.toList());
    }

    private FormFieldOption getFormFieldOption(AppSyncMode appSyncMode) {
        switch (appSyncMode) {
            case UPSERT:
                return new FormFieldOption(appSyncMode, "Upsert", "Update existing records and insert new records on the target object");
            case UPDATE:
                return new FormFieldOption(appSyncMode, "Update", "Update existing records and ignore new records on the target object");
            case INSERT:
                return new FormFieldOption(appSyncMode, "Insert", "Inserts events as new records on the target object");
            default:
                throw new CastledRuntimeException(String.format("Invalid app sync mode %s", appSyncMode));
        }
    }
}
