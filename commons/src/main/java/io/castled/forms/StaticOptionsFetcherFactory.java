package io.castled.forms;

import io.castled.forms.dtos.FormFieldOption;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

@Singleton
public class StaticOptionsFetcherFactory {

    private final Map<String, StaticOptionsFetcher> staticOptionsFetchers;

    @Inject
    public StaticOptionsFetcherFactory(Map<String, StaticOptionsFetcher> staticOptionsFetchers) {
        this.staticOptionsFetchers = staticOptionsFetchers;
    }

    public List<FormFieldOption> getOptions(String optionsRef) {
        return staticOptionsFetchers.get(optionsRef).getOptions();
    }
}