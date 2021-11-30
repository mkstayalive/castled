package io.castled.apps.connectors.marketo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MarketoSyncError {

    private Integer msgIdx;
    private String errorCode;
    private String message;
}
