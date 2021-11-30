package io.castled.apps.connectors.salesforce.client.dtos;

import lombok.Data;

import java.util.List;

@Data
public class SFDCObjectDetails {
    private List<SFDCObjectField> fields;
    private boolean custom;
    private boolean updateable;
}
