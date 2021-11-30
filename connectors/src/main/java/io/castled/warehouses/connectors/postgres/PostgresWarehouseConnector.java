package io.castled.warehouses.connectors.postgres;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.constants.ConnectorExecutionConstants;
import io.castled.exceptions.connect.ConnectException;
import io.castled.exceptions.connect.ConnectionError;
import io.castled.jdbc.JdbcConnectionManager;
import io.castled.jdbc.JdbcConnectionType;
import io.castled.models.QueryResults;
import io.castled.schema.models.RecordSchema;
import io.castled.utils.JDBCUtils;
import io.castled.warehouses.TableProperties;
import io.castled.warehouses.WarehouseDataPoller;
import io.castled.warehouses.WarehouseSyncFailureListener;
import io.castled.warehouses.jdbc.JdbcWarehouseConnector;
import io.castled.warehouses.models.WarehousePollContext;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Singleton
public class PostgresWarehouseConnector extends JdbcWarehouseConnector<PostgresWarehouseConfig> {

    private final PostgresResultSetSchemaMapper resultSetSchemaMapper;
    private final JdbcConnectionManager jdbcConnectionManager;
    private final PostgresClient postgresClient;

    @Inject
    public PostgresWarehouseConnector(PostgresResultSetSchemaMapper resultSetSchemaMapper,
                                      JdbcConnectionManager jdbcConnectionManager,
                                      PostgresClient postgresClient) {
        this.resultSetSchemaMapper = resultSetSchemaMapper;
        this.jdbcConnectionManager = jdbcConnectionManager;
        this.postgresClient = postgresClient;
    }

    @Override
    public Class<PostgresWarehouseConfig> getConfigType() {
        return PostgresWarehouseConfig.class;
    }

    @Override
    public TableProperties getSnapshotTableProperties(List<String> recordIdKeys) {
        return new PostgresTableProperties(recordIdKeys);
    }

    @Override
    public void testConnectionForDataPoll(PostgresWarehouseConfig config) throws ConnectException {
        try {
            try (Connection connection = getConnection(config)) {
                List<String> schemas = this.postgresClient.listSchemas(connection);
                if (!schemas.contains(ConnectorExecutionConstants.CASTLED_CONTAINER)) {
                    throw new ConnectException(ConnectionError.INCOMPLETE_SETUP,
                            String.format("%s schema does not exist or the user does not have sufficient permissions to access it." +
                                            "Please create the castled schema and/or give relevant permissions as mentioned in our doc",
                                    ConnectorExecutionConstants.CASTLED_CONTAINER));
                }
            }
        } catch (Exception e) {
            throw new ConnectException(ConnectionError.UNKNOWN, Optional.ofNullable(e.getMessage()).orElse("Unknown Error"));
        }

    }

    public Connection getConnection(PostgresWarehouseConfig config) throws SQLException {
        return jdbcConnectionManager.getTunneledConnection(getConnectionType(), config.getDbUser(), config.getDbPassword(), config.getDbName(),
                null, config.getServerHost(), config.getServerPort(), config.getSSHTunnelParams(), new Properties());
    }

    @Override
    public JdbcConnectionType getConnectionType() {
        return JdbcConnectionType.POSTGRES;
    }

    @Override
    public WarehouseDataPoller getDataPoller() {
        return ObjectRegistry.getInstance(PostgresDataPoller.class);
    }

    @Override
    public RecordSchema getQuerySchema(PostgresWarehouseConfig postgresWarehouseConfig, String query) throws Exception {
        try (Connection connection = getConnection(postgresWarehouseConfig)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                return resultSetSchemaMapper.getSchema(preparedStatement.getMetaData());
            }
        }
    }

    @Override
    public WarehouseSyncFailureListener syncFailureListener(WarehousePollContext warehousePollContext) throws Exception {
        return new PostgresSyncFailureListener(warehousePollContext);
    }

    @Override
    public void restartPoll(String pipelineUUID, PostgresWarehouseConfig postgresWarehouseConfig) throws Exception {
        try (Connection connection = getConnection(postgresWarehouseConfig)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("drop table if exists %s", ConnectorExecutionConstants.getQualifiedCommittedSnapshot(pipelineUUID)));
            }
        }
    }

    @Override
    public QueryResults previewQuery(String query, PostgresWarehouseConfig postgresWarehouseConfig, int maxRows) throws Exception {
        try (Connection connection = getConnection(postgresWarehouseConfig)) {
            try (Statement statement = connection.createStatement()) {
                statement.setMaxRows(maxRows);
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    return JDBCUtils.getQueryResults(resultSet);
                }
            }
        }
    }
}
