package io.castled.apps.connectors.mailchimp.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchOperationsRequest {
    private List<BatchOperation> operations;
}
