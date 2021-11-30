package io.castled.warehouses;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.apps.ExternalApp;
import io.castled.apps.dtos.AppSyncConfigDTO;
import io.castled.forms.dtos.FormFieldOption;
import io.castled.models.Warehouse;
import io.castled.optionsfetchers.appsync.AppSyncOptionsFetcher;
import io.castled.schema.models.RecordSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class WarehouseColumnFetcher implements AppSyncOptionsFetcher {

    private final WarehouseService warehouseService;

    @Inject
    public WarehouseColumnFetcher(WarehouseService warehouseService){
        this.warehouseService = warehouseService;
    }

    @Override
    public List<FormFieldOption> getOptions(AppSyncConfigDTO config, ExternalApp externalApp) {
        try {
            RecordSchema recordSchema = warehouseService.fetchSchema(config.getWarehouseId(),config.getSourceQuery());
            return recordSchema.getFieldSchemas().stream().map( e -> new FormFieldOption(e.getName(),e.getName(),e.getName())).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error while fetching schema : "+e);
        }
        return Collections.emptyList();
    }
}