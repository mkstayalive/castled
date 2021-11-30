package io.castled.optionsfetchers;

import io.castled.forms.dtos.FormFieldOption;
import io.castled.warehouses.WarehouseConfig;

import java.util.List;

public interface WarehouseConfigOptionFetcher {

    List<FormFieldOption> getOptions(WarehouseConfig warehouseConfig);
}
