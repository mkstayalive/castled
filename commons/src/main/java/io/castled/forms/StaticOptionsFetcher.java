package io.castled.forms;

import io.castled.forms.dtos.FormFieldOption;

import java.util.List;

public interface StaticOptionsFetcher {

    List<FormFieldOption> getOptions();

}
