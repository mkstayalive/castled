package io.castled.warehouses.connectors.bigquery;

import io.castled.forms.StaticOptionsFetcher;
import io.castled.forms.dtos.FormFieldOption;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class BQLocationsFetcher implements StaticOptionsFetcher {
    @Override
    public List<FormFieldOption> getOptions() {
        return Arrays.stream(GCPRegions.values()).map(gcpRegion ->
                new FormFieldOption(gcpRegion.getRegionName(),
                        String.format("%s (%s)", normalizeRegion(gcpRegion.name()), gcpRegion.getRegionName()))).collect(Collectors.toList());
    }

    private String normalizeRegion(String name) {
        return WordUtils.capitalizeFully(name.replaceAll("_", " "));
    }
}
