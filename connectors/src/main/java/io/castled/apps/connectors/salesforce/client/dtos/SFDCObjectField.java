package io.castled.apps.connectors.salesforce.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SFDCObjectField {
    private String name;
    private String type;
    private String compoundFieldName;
    private boolean sortable;
    private int scale;
    private int precision;
    private String soapType;
    private boolean updateable;
    private long length;
    private boolean idLookup;
    private boolean externalId;
    private boolean unique;
    private boolean nillable;
}
