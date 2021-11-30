package io.castled.apps.connectors.mixpanel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileBulkUpdateResponse {

    private String status;
    private String error;
}
