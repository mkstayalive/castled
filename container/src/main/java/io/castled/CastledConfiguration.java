package io.castled;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.castled.apps.connectors.salesforce.SalesforceSinkConfig;
import io.castled.jarvis.scheduler.models.JarvisSchedulerConfig;
import io.castled.kafka.KafkaApplicationConfig;
import io.castled.models.JarvisTaskConfiguration;
import io.castled.models.RedisConfig;
import io.castled.warehouses.WarehouseConnectorConfig;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CastledConfiguration extends Configuration {

    private DataSourceFactory database = new DataSourceFactory();
    @NotNull
    private JarvisTaskConfiguration jarvisTaskConfig;
    @NotNull
    private JarvisSchedulerConfig jarvisSchedulerConfig;
    @NotNull
    private RedisConfig redisConfig;

    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

    @NotNull
    private KafkaApplicationConfig kafkaConfig;

    private WarehouseConnectorConfig warehouseConnectorConfig = new WarehouseConnectorConfig();

    private SalesforceSinkConfig salesforceSinkConfig = new SalesforceSinkConfig();


}
