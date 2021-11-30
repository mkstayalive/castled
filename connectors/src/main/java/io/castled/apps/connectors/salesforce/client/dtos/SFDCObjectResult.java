package io.castled.apps.connectors.salesforce.client.dtos;

import lombok.Data;

import java.util.List;

@Data
public class SFDCObjectResult {
    private List<SFDCObject> sObjects;
}
