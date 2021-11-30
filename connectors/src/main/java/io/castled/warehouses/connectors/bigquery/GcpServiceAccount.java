package io.castled.warehouses.connectors.bigquery;

import io.castled.commons.models.ServiceAccountDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GcpServiceAccount {

    private Long id;
    private Long userId;
    private ServiceAccountDetails serviceAccountDetails;
}
