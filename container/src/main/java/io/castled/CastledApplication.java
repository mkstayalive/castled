package io.castled;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.castled.apps.ConnectorsModule;
import io.castled.commands.CastledServerCommand;
import io.castled.jarvis.JarvisModule;
import io.castled.models.users.User;
import io.castled.resources.*;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jdbi.v3.core.Jdbi;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.ws.rs.client.Client;
import java.util.EnumSet;

public class CastledApplication extends Application<CastledConfiguration> {

    @Inject
    private OAuthResource oAuthResource;

    @Inject
    private ExternalAppResource externalAppResource;

    @Inject
    private CastledLifecycleManager lifecycleManager;

    @Inject
    private WarehouseResource warehouseResource;

    @Inject
    private AppShutdownHandler appShutdownHandler;

    @Inject
    private PipelineRunResource pipelineRunResource;

    @Inject
    private PipelineResource pipelineResource;

    @Inject
    private CastledAuthFilter castledAuthFilter;

    @Inject
    private CastledAppManager castledAppManager;

    @Inject
    private UsersResource usersResource;

    public static void main(String[] args) throws Exception {
        new CastledApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<CastledConfiguration> bootstrap) {
        bootstrap.addCommand(new CastledServerCommand(this));
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)));
        bootstrap.getObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void run(CastledConfiguration castledConfiguration, Environment environment) throws Exception {
        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, castledConfiguration.getDatabase(), "mysql");
        final Client client = new JerseyClientBuilder(environment).using(castledConfiguration.getJerseyClient())
                .withProvider(MultiPartFeature.class).build(getName());

        // start http client logging
        // Feature feature = new LoggingFeature(Logger.getGlobal(), Level.INFO, LoggingFeature.Verbosity.PAYLOAD_ANY, 1000000);
        // client.register(feature);

        Injector injector = Guice.createInjector(new CastledModule(jdbi, client, castledConfiguration),
                new JarvisModule());
        Guice.createInjector(Modules.override(new ConnectorsModule(castledConfiguration.getWarehouseConnectorConfig()))
                .with(new CastledModule(jdbi, client, castledConfiguration)), new JarvisModule());
        enableCors(environment);
        ObjectRegistry.setInjector(injector);
        injector.injectMembers(this);
        this.castledAppManager.initializeAppComponents();
        environment.jersey().register(oAuthResource);
        environment.jersey().register(externalAppResource);
        environment.jersey().register(warehouseResource);
        environment.jersey().register(pipelineResource);
        environment.jersey().register(pipelineRunResource);
        environment.jersey().register(usersResource);
        environment.lifecycle().manage(lifecycleManager);

        environment.jersey().register(new AuthDynamicFeature(castledAuthFilter));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> appShutdownHandler.handleShutdown()));
    }

    private void enableCors(Environment environment) {
        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type, Authorization, Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

    }
}
