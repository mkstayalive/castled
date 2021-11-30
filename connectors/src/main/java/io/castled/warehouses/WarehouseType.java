package io.castled.warehouses;

import com.google.common.collect.ImmutableMap;
import io.castled.commons.models.AccessType;
import io.castled.constants.ConnectorConstants;

import java.util.Map;

public enum WarehouseType {

    REDSHIFT(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.PASSWORD)
            .put(ConnectorConstants.TITLE, "Redshift")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/warehouses/redshift.png")
            .put(ConnectorConstants.DOC_URL, "https://docs.castled.io/getting-started/Sources/configure-redshift")

            .build()),
    SNOWFLAKE(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.PASSWORD)
            .put(ConnectorConstants.TITLE, "Snowflake")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/warehouses/snowflake.png")
            .put(ConnectorConstants.DOC_URL, "https://docs.castled.io/getting-started/Sources/configure-snowflake")
            .build()),
    POSTGRES(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.PASSWORD)
            .put(ConnectorConstants.TITLE, "Postgres")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/warehouses/postgres.png")
            .put(ConnectorConstants.DOC_URL, "https://docs.castled.io/getting-started/Sources/configure-postgres")
            .build()),
    BIGQUERY(ImmutableMap.<String, Object>builder()
            .put(ConnectorConstants.ACCESS_TYPE, AccessType.PASSWORD)
            .put(ConnectorConstants.TITLE, "BigQuery")
            .put(ConnectorConstants.LOGO_URL, "https://cdn.castled.io/warehouses/bigquery.png")
            .put(ConnectorConstants.DOC_URL, "https://docs.castled.io/getting-started/Sources/configure-bigquery")
            .build());


    WarehouseType(Map<String, Object> properties) {
        this.properties = properties;
    }

    public AccessType getAccessType() {
        return (AccessType) properties.get(ConnectorConstants.ACCESS_TYPE);
    }

    public String title() {
        return (String) properties.get(ConnectorConstants.TITLE);
    }

    public String getLogoUrl() {
        return (String) properties.get(ConnectorConstants.LOGO_URL);
    }

    public String getDocUrl() {
        return (String) properties.get(ConnectorConstants.DOC_URL);
    }


    private final Map<String, Object> properties;
}
