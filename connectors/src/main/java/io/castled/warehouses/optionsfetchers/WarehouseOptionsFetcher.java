package io.castled.warehouses.optionsfetchers;

import io.castled.forms.dtos.FormFieldOption;
import io.castled.warehouses.WarehouseConfig;

import java.util.List;

public interface WarehouseOptionsFetcher {

    List<FormFieldOption> getFieldOptions(WarehouseConfig warehouseConfig);
}
