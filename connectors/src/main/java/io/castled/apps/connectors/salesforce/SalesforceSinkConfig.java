package io.castled.apps.connectors.salesforce;

import lombok.Data;

@Data
public class SalesforceSinkConfig {

    //in mbs
    private int requestBufferThreshold = 80;

    private int uploadTimeoutMins = 30;
}
