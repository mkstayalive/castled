package io.castled.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldMapping {
    private String warehouseField;
    private String appField;
    private boolean skipped;
}
