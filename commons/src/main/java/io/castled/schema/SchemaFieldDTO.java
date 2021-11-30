package io.castled.schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemaFieldDTO {
    private String fieldName;
    private String type;
    private boolean optional;
}
