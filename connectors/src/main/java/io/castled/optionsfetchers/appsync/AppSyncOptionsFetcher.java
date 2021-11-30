package io.castled.optionsfetchers.appsync;

import io.castled.apps.ExternalApp;
import io.castled.apps.dtos.AppSyncConfigDTO;
import io.castled.forms.dtos.FormFieldOption;

import java.util.List;

public interface AppSyncOptionsFetcher {

    List<FormFieldOption> getOptions(AppSyncConfigDTO config, ExternalApp externalApp);
}
