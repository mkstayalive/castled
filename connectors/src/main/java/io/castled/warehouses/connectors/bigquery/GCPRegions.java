package io.castled.warehouses.connectors.bigquery;

import lombok.Getter;

public enum GCPRegions {

    UNITED_STATES("US"),
    EUROPEAN_UNION("EU"),
    DELHI("asia-south2"),
    MUMBAI("asia-south1");

    @Getter
    private final String regionName;

    GCPRegions(String regionName) {
        this.regionName = regionName;

    }

}
