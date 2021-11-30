package io.castled.warehouses.dtos;

import io.castled.commons.models.AccessType;
import io.castled.warehouses.WarehouseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WarehouseTypeDTO {

    private WarehouseType value;
    private String title;
    private AccessType accessType;
    private String logoUrl;
    private String docUrl;
    private long count;
}
