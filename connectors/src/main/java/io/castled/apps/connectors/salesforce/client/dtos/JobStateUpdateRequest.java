package io.castled.apps.connectors.salesforce.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobStateUpdateRequest {

    private JobState state;
}
