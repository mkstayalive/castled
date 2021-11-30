package io.castled.warehouses;

import io.castled.forms.FormUtils;
import io.castled.forms.dtos.FormFieldsDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseWarehouseConnector<CONFIG extends WarehouseConfig> implements WarehouseConnector<CONFIG> {

    public FormFieldsDTO getFormFields() {
        Class<CONFIG> configClass = getConfigType();
        return FormUtils.getFormFields(configClass);
    }

    public abstract Class<CONFIG> getConfigType();
}
