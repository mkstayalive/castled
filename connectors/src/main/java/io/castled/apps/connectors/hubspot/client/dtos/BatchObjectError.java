package io.castled.apps.connectors.hubspot.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchObjectError {

    private String status;
    private String message;
    private String category;
}
