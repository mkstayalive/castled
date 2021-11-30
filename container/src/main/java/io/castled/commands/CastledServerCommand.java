package io.castled.commands;

import com.mysql.cj.jdbc.MysqlDataSource;
import io.castled.CastledApplication;
import io.castled.CastledConfiguration;
import io.castled.ObjectRegistry;
import io.castled.services.UsersService;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.flywaydb.core.Flyway;

public class CastledServerCommand extends ServerCommand<CastledConfiguration> {

    public CastledServerCommand(CastledApplication castledApplication) {
        super(castledApplication, "castled-server", "Runs the castled server");
    }

    protected void run(Environment environment, Namespace namespace, CastledConfiguration configuration) throws Exception {
        runMigrations(configuration);
        super.run(environment, namespace, configuration);
    }

    private void runMigrations(CastledConfiguration configuration) {
        Flyway flyway = new Flyway();
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setURL(configuration.getDatabase().getUrl());
        flyway.setDataSource(mysqlDataSource);
        flyway.setLocations("migration");
        flyway.migrate();

        //create test team and user if required
        UsersService usersService = ObjectRegistry.getInstance(UsersService.class);
        if (usersService.getUser() == null) {
            usersService.createTestTeamAndUser();
        }

    }
}
