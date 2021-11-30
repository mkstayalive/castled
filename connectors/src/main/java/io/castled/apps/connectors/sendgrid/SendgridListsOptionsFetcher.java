package io.castled.apps.connectors.sendgrid;

import io.castled.apps.ExternalApp;
import io.castled.apps.connectors.sendgrid.dtos.ContactList;
import io.castled.apps.dtos.AppSyncConfigDTO;
import io.castled.forms.dtos.FormFieldOption;
import io.castled.optionsfetchers.appsync.AppSyncOptionsFetcher;

import java.util.List;
import java.util.stream.Collectors;

public class SendgridListsOptionsFetcher implements AppSyncOptionsFetcher {

    @Override
    public List<FormFieldOption> getOptions(AppSyncConfigDTO config, ExternalApp externalApp) {
        SendgridRestClient sendgridRestClient = new SendgridRestClient((SendgridAppConfig) externalApp.getConfig());
        List<ContactList> contactLists = sendgridRestClient.getContactLists();
        return contactLists.stream().map(listRef -> new FormFieldOption(listRef.getId(), listRef.getName()))
                .collect(Collectors.toList());
    }
}
