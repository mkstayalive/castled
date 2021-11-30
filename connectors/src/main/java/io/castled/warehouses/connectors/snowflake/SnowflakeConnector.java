package io.castled.warehouses.connectors.snowflake;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.constants.ConnectorExecutionConstants;
import io.castled.exceptions.connect.ConnectException;
import io.castled.exceptions.connect.ConnectionError;
import io.castled.jdbc.JdbcConnectionManager;
import io.castled.jdbc.JdbcConnectionType;
import io.castled.jdbc.snowflake.SnowflakeQueryHelper;
import io.castled.models.QueryResults;
import io.castled.schema.ResultSetSchemaMapper;
import io.castled.schema.models.RecordSchema;
import io.castled.utils.JDBCUtils;
import io.castled.warehouses.TableProperties;
import io.castled.warehouses.WarehouseDataPoller;
import io.castled.warehouses.WarehouseSyncFailureListener;
import io.castled.warehouses.jdbc.JdbcWarehouseConnector;
import io.castled.warehouses.models.WarehousePollContext;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Singleton
@Slf4j
public class SnowflakeConnector extends JdbcWarehouseConnector<SnowflakeWarehouseConfig> {

    private final SnowflakeQueryHelper snowflakeQueryHelper;
    private final ResultSetSchemaMapper resultSetSchemaMapper;
    private final SnowflakeClient snowflakeClient;


    @Inject
    public SnowflakeConnector(SnowflakeQueryHelper snowflakeQueryHelper,
                              ResultSetSchemaMapper resultSetSchemaMapper, SnowflakeClient snowflakeClient) {

        this.snowflakeQueryHelper = snowflakeQueryHelper;
        this.resultSetSchemaMapper = resultSetSchemaMapper;
        this.snowflakeClient = snowflakeClient;
    }

    @Override
    public void testConnectionForDataPoll(SnowflakeWarehouseConfig config) throws ConnectException {
        if (!validateAccountName(config.getAccountName())) {
            throw new ConnectException(ConnectionError.INVALID_CONFIG,
                    String.format("Account name should of the format [%s].[%s].[%s]", "account_locator", "region", "cloud_provider"));
        }
        try {
            try (Connection connection = getConnection(config)) {
                List<String> schemas = this.snowflakeClient.listSchemas(connection);
                if (!schemas.contains(ConnectorExecutionConstants.CASTLED_CONTAINER.toUpperCase())) {
                    throw new ConnectException(ConnectionError.INCOMPLETE_SETUP,
                            String.format("%s schema does not exist. Please create the castled schema as mentioned in our doc",
                                    ConnectorExecutionConstants.CASTLED_CONTAINER));
                }
            }
        } catch (Exception e) {
            log.error("Test connection failed for snowflake warehouse", e);
            throw new ConnectException(ConnectionError.UNKNOWN, Optional.ofNullable(e.getMessage()).orElse("Unknown Error"));
        }
    }

    private boolean validateAccountName(String accountName) {
        String[] tokens = accountName.split("\\.");
        return tokens.length == 3;

    }

    @Override
    public WarehouseDataPoller getDataPoller() {
        return ObjectRegistry.getInstance(SnowflakeDataPoller.class);
    }

    @Override
    public RecordSchema getQuerySchema(SnowflakeWarehouseConfig snowflakeWarehouseConfig, String query) throws Exception {
        try (Connection connection = getConnection(snowflakeWarehouseConfig)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                return resultSetSchemaMapper.getSchema(preparedStatement.getMetaData());
            }
        }
    }

    @Override
    public WarehouseSyncFailureListener syncFailureListener(WarehousePollContext warehousePollContext) throws Exception {
        return new SnowflakeSyncFailureListener(warehousePollContext);
    }

    @Override
    public TableProperties getSnapshotTableProperties(List<String> recordIdKeys) {
        return null;
    }

    @Override
    public void restartPoll(String pipelineUUID, SnowflakeWarehouseConfig snowflakeWarehouseConfig) throws Exception {
        try (Connection connection = getConnection(snowflakeWarehouseConfig)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("drop table if exists %s", ConnectorExecutionConstants.getQualifiedCommittedSnapshot(pipelineUUID)));
            }
        }
    }

    @Override
    public QueryResults previewQuery(String query, SnowflakeWarehouseConfig snowflakeWarehouseConfig, int maxRows) throws Exception {
        try (Connection connection = getConnection(snowflakeWarehouseConfig)) {
            try (Statement statement = connection.createStatement()) {
                statement.setMaxRows(maxRows);
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    return JDBCUtils.getQueryResults(resultSet);
                }
            }
        }
    }

    @Override
    public Connection getConnection(SnowflakeWarehouseConfig config) throws SQLException {
        JdbcConnectionManager jdbcConnectionManager = ObjectRegistry.getInstance(JdbcConnectionManager.class);
        return jdbcConnectionManager.getTunneledConnection(getConnectionType(), config.getDbUser(), config.getDbPassword(), config.getDbName(),
                config.getSchemaName(), config.getDbHost(), config.getDbPort(), null,
                snowflakeQueryHelper.getConnectionProperties(config.getWarehouseName(), config.getDbName(), config.getSchemaName())
        );
    }

    @Override
    public JdbcConnectionType getConnectionType() {
        return JdbcConnectionType.SNOWFLAKE;
    }

    @Override
    public Class<SnowflakeWarehouseConfig> getConfigType() {
        return SnowflakeWarehouseConfig.class;
    }
}
