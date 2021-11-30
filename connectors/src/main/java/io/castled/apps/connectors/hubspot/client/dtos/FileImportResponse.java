package io.castled.apps.connectors.hubspot.client.dtos;

import lombok.Data;

@Data
public class FileImportResponse {
    private ImportState state;
    private String id;

}
