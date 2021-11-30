package io.castled.apps.connectors.hubspot.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ColumnMapping {
    private boolean ignored;
    private String columnName;
    private IdColumnType idColumnType;
    private String propertyName;
    private String foreignKeyType;
    private String columnObjectTypeId;
    private boolean associationIdentifierColumn;
}
