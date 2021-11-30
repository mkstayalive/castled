package io.castled.models;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class CastledDataMapping {

    private List<String> primaryKeys;

    private List<FieldMapping> fieldMappings;

    public Map<String, String> getMappingForAppFields(List<String> appFields) {
        return fieldMappings.stream().filter(fieldMapping -> appFields.contains(fieldMapping.getAppField()))
                .collect(Collectors.toMap(FieldMapping::getAppField, FieldMapping::getWarehouseField));
    }

    public Map<String, String> appWarehouseMapping() {
        return fieldMappings.stream().filter(fieldMapping -> !fieldMapping.isSkipped())
                .collect(Collectors.toMap(FieldMapping::getAppField, FieldMapping::getWarehouseField));
    }

    public Map<String, String> warehouseAppMapping() {
        return fieldMappings.stream().filter(fieldMapping -> !fieldMapping.isSkipped())
                .collect(Collectors.toMap(FieldMapping::getWarehouseField, FieldMapping::getAppField));
    }

    public void addAdditionalMappings(List<FieldMapping> additionalMappings){
        fieldMappings.addAll(additionalMappings);
    }

    public void addAdditionalMapping(FieldMapping additionalMapping){
        fieldMappings.add(additionalMapping);
    }
}
