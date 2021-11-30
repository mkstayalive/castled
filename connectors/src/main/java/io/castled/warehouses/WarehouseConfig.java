package io.castled.warehouses;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.castled.warehouses.connectors.bigquery.BigQueryWarehouseConfig;
import io.castled.warehouses.connectors.postgres.PostgresWarehouseConfig;
import io.castled.warehouses.connectors.redshift.RedshiftWarehouseConfig;
import io.castled.warehouses.connectors.snowflake.SnowflakeWarehouseConfig;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RedshiftWarehouseConfig.class, name = "REDSHIFT"),
        @JsonSubTypes.Type(value = SnowflakeWarehouseConfig.class, name = "SNOWFLAKE"),
        @JsonSubTypes.Type(value = PostgresWarehouseConfig.class, name = "POSTGRES"),
        @JsonSubTypes.Type(value = BigQueryWarehouseConfig.class, name = "BIGQUERY")})

@Getter
@Setter
public abstract class WarehouseConfig {

    private WarehouseType type;


}
