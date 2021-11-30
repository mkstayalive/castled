package io.castled;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import io.castled.apps.ConnectorsModule;
import io.castled.apps.ExternalAppConnector;
import io.castled.apps.ExternalAppType;
import io.castled.apps.connectors.activecampaign.ActiveCampaignAppConnector;
import io.castled.apps.connectors.customerio.CustomerIOAppConnector;
import io.castled.apps.connectors.googleads.GoogleAdsAppConnector;
import io.castled.apps.connectors.googlepubsub.GooglePubSubAppConnector;
import io.castled.apps.connectors.hubspot.HubspotAppConnector;
import io.castled.apps.connectors.intercom.IntercomAppConnector;
import io.castled.apps.connectors.kafka.KafkaAppConnector;
import io.castled.apps.connectors.mailchimp.MailchimpAppConnector;
import io.castled.apps.connectors.marketo.MarketoAppConnector;
import io.castled.apps.connectors.mixpanel.MixpanelAppConnector;
import io.castled.apps.connectors.salesforce.SalesforceAppConnector;
import io.castled.apps.connectors.sendgrid.SendgridAppConnector;
import io.castled.events.pipelineevents.*;
import io.castled.interceptors.Retry;
import io.castled.interceptors.RetryInterceptor;
import io.castled.jarvis.DummyTaskExecutor;
import io.castled.jarvis.JarvisTaskType;
import io.castled.jarvis.scheduler.JarvisGlobalCronJob;
import io.castled.jarvis.scheduler.models.JarvisSchedulerConfig;
import io.castled.jarvis.taskmanager.TaskExecutor;
import io.castled.jarvis.taskmanager.models.JarvisKafkaConfig;
import io.castled.jarvis.taskmanager.models.JarvisTaskClientConfig;
import io.castled.jarvis.taskmanager.models.TaskGroup;
import io.castled.kafka.KafkaApplicationConfig;
import io.castled.kafka.producer.CastledKafkaProducer;
import io.castled.kafka.producer.KafkaProducerConfiguration;
import io.castled.models.JarvisTaskConfiguration;
import io.castled.models.RedisConfig;
import io.castled.optionsfetchers.appsync.AppSyncOptionsFetcher;
import io.castled.pipelines.PipelineExecutor;
import io.castled.utils.TimeUtils;
import io.castled.warehouses.QueryPreviewExecutor;
import io.castled.warehouses.WarehouseColumnFetcher;
import org.jdbi.v3.core.Jdbi;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.ws.rs.client.Client;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({"rawTypes"})
public class CastledModule extends AbstractModule {

    private final Jdbi jdbi;
    private final CastledConfiguration castledConfiguration;
    private final Client jerseyClient;

    public CastledModule(Jdbi jdbi, Client jerseyClient, CastledConfiguration castledConfiguration) {
        this.jdbi = jdbi;
        this.castledConfiguration = castledConfiguration;
        this.jerseyClient = jerseyClient;
    }

    @Override
    protected void configure() {

        install(new ConnectorsModule(castledConfiguration.getWarehouseConnectorConfig()));
        bindTaskExecutors();
        bindScheduledJobs();
        bindAppConnectors();
        bindPipelineEventHandlers();
        bindInterceptors();
        bindAppSyncOptions();
    }

    private void bindAppSyncOptions() {
        MapBinder<String, AppSyncOptionsFetcher> optionFetchers = MapBinder.newMapBinder(binder(),
                String.class, AppSyncOptionsFetcher.class);
        optionFetchers.addBinding(OptionsReferences.WAREHOUSE_COLUMNS).to(WarehouseColumnFetcher.class);
    }

    private void bindInterceptors() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Retry.class), new RetryInterceptor());
    }

    private void bindPipelineEventHandlers() {
        MapBinder<PipelineEventType, PipelineEventsHandler> pipelineEventHandlers = MapBinder.newMapBinder(binder(),
                PipelineEventType.class, PipelineEventsHandler.class);
        pipelineEventHandlers.addBinding(PipelineEventType.PIPELINE_CREATED).to(PipelineCreateEventsHandler.class);
        pipelineEventHandlers.addBinding(PipelineEventType.PIPELINE_DELETED).to(PipelineDeleteEventsHandler.class);
        pipelineEventHandlers.addBinding(PipelineEventType.PIPELINE_SCHEDULE_CHANGED).to(PipelineScheduleChangeEventsHandler.class);
    }


    private void bindAppConnectors() {
        MapBinder<ExternalAppType, ExternalAppConnector> externalAppConnectorMapping = MapBinder.newMapBinder(binder(),
                ExternalAppType.class, ExternalAppConnector.class);
        externalAppConnectorMapping.addBinding(ExternalAppType.SALESFORCE).to(SalesforceAppConnector.class);
        externalAppConnectorMapping.addBinding(ExternalAppType.HUBSPOT).to(HubspotAppConnector.class);
        externalAppConnectorMapping.addBinding(ExternalAppType.INTERCOM).to(IntercomAppConnector.class);
        externalAppConnectorMapping.addBinding(ExternalAppType.GOOGLEADS).to(GoogleAdsAppConnector.class);
        externalAppConnectorMapping.addBinding(ExternalAppType.MAILCHIMP).to(MailchimpAppConnector.class);
        externalAppConnectorMapping.addBinding(ExternalAppType.SENDGRID).to(SendgridAppConnector.class);
        externalAppConnectorMapping.addBinding(ExternalAppType.ACTIVECAMPAIGN).to(ActiveCampaignAppConnector.class);
        externalAppConnectorMapping.addBinding(ExternalAppType.MARKETO).to(MarketoAppConnector.class);
        externalAppConnectorMapping.addBinding(ExternalAppType.KAFKA).to(KafkaAppConnector.class);
        externalAppConnectorMapping.addBinding(ExternalAppType.CUSTOMERIO).to(CustomerIOAppConnector.class);
        externalAppConnectorMapping.addBinding(ExternalAppType.GOOGLEPUBSUB).to(GooglePubSubAppConnector.class);
        externalAppConnectorMapping.addBinding(ExternalAppType.MIXPANEL).to(MixpanelAppConnector.class);
    }


    private void bindScheduledJobs() {
        Multibinder<JarvisGlobalCronJob> multiBinder = Multibinder.newSetBinder(binder(), JarvisGlobalCronJob.class);
    }

    private void bindTaskExecutors() {
        MapBinder<JarvisTaskType, TaskExecutor> taskExecutorMapping = MapBinder.newMapBinder(binder(),
                JarvisTaskType.class, TaskExecutor.class);
        taskExecutorMapping.addBinding(JarvisTaskType.DUMMY).to(DummyTaskExecutor.class);
        taskExecutorMapping.addBinding(JarvisTaskType.PIPELINE_RUN).to(PipelineExecutor.class);
        taskExecutorMapping.addBinding(JarvisTaskType.PREVIEW_QUERY).to(QueryPreviewExecutor.class);
    }

    @Provides
    @Singleton
    public Jdbi providesJdbi() {
        return jdbi;
    }

    @Provides
    @Singleton
    public Client providesJerseyClient() {
        return jerseyClient;
    }

    @Provides
    @Singleton
    public JedisPool providesJedisPool() {
        RedisConfig redisConfig = castledConfiguration.getRedisConfig();
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxIdle(20);
        jedisPoolConfig.setMaxWaitMillis(TimeUtils.secondsToMillis(10));
        return new JedisPool(jedisPoolConfig, redisConfig.getHost(), redisConfig.getPort());
    }

    @Provides
    @Singleton
    public JarvisTaskConfiguration providesJarvisTaskConfig() {
        return castledConfiguration.getJarvisTaskConfig();
    }

    @Provides
    @Singleton
    @Inject
    public JarvisTaskClientConfig providesJarvisClientConfig(JedisPool jedisPool, Jdbi jdbi, Map<JarvisTaskType, TaskExecutor> taskExecutors,
                                                             JarvisTaskConfiguration jarvisTaskConfiguration) {
        JarvisKafkaConfig jarvisKafkaConfig = JarvisKafkaConfig.builder()
                .bootstrapServers(castledConfiguration.getKafkaConfig().getBootstrapServers()).consumerCount(3)
                .build();

        Map<String, TaskExecutor> jarvisTaskExecutors =
                taskExecutors.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
        List<TaskGroup> taskGroups = jarvisTaskConfiguration.getGroupConfig().stream()
                .map(groupConfig -> new TaskGroup(groupConfig.getGroup(), groupConfig.getWorkerCount(), jarvisTaskExecutors))
                .collect(Collectors.toList());
        return new JarvisTaskClientConfig(jedisPool, jdbi, jarvisKafkaConfig, taskGroups,
                jarvisTaskConfiguration.getPriorityCoolDownMins());
    }

    @Provides
    @Singleton
    public JarvisSchedulerConfig providesSchedulerConfig() {
        return castledConfiguration.getJarvisSchedulerConfig();
    }

    @Provides
    @Singleton
    public CastledKafkaProducer providesCastledKafkaProducer() {
        return new CastledKafkaProducer(KafkaProducerConfiguration.builder()
                .bootstrapServers(castledConfiguration.getKafkaConfig().getBootstrapServers()).build());

    }

    @Provides
    @Singleton
    public CastledConfiguration providesCastledConfiguration() {
        return castledConfiguration;
    }

    @Provides
    @Singleton
    public KafkaApplicationConfig kafkaApplicationConfig() {
        return castledConfiguration.getKafkaConfig();
    }
}
