package io.castled.warehouses.connectors.redshift;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.constants.ConnectorExecutionConstants;
import io.castled.exceptions.connect.ConnectException;
import io.castled.exceptions.connect.ConnectionError;
import io.castled.jdbc.JdbcConnectionManager;
import io.castled.jdbc.JdbcConnectionType;
import io.castled.models.QueryResults;
import io.castled.schema.ResultSetSchemaMapper;
import io.castled.schema.models.RecordSchema;
import io.castled.utils.JDBCUtils;
import io.castled.warehouses.TableProperties;
import io.castled.warehouses.WarehouseDataPoller;
import io.castled.warehouses.WarehouseSyncFailureListener;
import io.castled.warehouses.jdbc.JdbcWarehouseConnector;
import io.castled.warehouses.models.WarehousePollContext;
import org.apache.commons.collections4.CollectionUtils;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Singleton
public class RedshiftConnector extends JdbcWarehouseConnector<RedshiftWarehouseConfig> {

    private final RedshiftDataPoller redshiftDataPoller;
    private final ResultSetSchemaMapper resultSetSchemaMapper;
    private final RedshiftClient redshiftClient;

    @Inject
    public RedshiftConnector(RedshiftDataPoller redshiftDataPoller, ResultSetSchemaMapper resultSetSchemaMapper,
                             RedshiftClient redshiftClient) {
        this.redshiftDataPoller = redshiftDataPoller;
        this.resultSetSchemaMapper = resultSetSchemaMapper;
        this.redshiftClient = redshiftClient;
    }


    public Connection getConnection(RedshiftWarehouseConfig config) throws SQLException {
        JdbcConnectionManager jdbcConnectionManager = ObjectRegistry.getInstance(JdbcConnectionManager.class);
        return jdbcConnectionManager.getTunneledConnection(getConnectionType(), config.getDbUser(), config.getDbPassword(), config.getDbName(),
                null, config.getServerHost(), config.getServerPort(), config.getSSHTunnelParams(), new Properties());
    }

    @Override
    public void testConnectionForDataPoll(RedshiftWarehouseConfig config) throws ConnectException {
        try {
            try (Connection connection = getConnection(config)) {
                List<String> schemas = this.redshiftClient.listSchemas(connection);
                if (!schemas.contains(ConnectorExecutionConstants.CASTLED_CONTAINER)) {
                    throw new ConnectException(ConnectionError.INCOMPLETE_SETUP,
                            String.format("%s schema does not exist. Please create the castled schema as mentioned in our doc",
                                    ConnectorExecutionConstants.CASTLED_CONTAINER));
                }
            }
        } catch (Exception e) {
            throw new ConnectException(ConnectionError.UNKNOWN, Optional.ofNullable(e.getMessage()).orElse("Unknown Error"));
        }
    }


    @Override
    public WarehouseDataPoller getDataPoller() {
        return redshiftDataPoller;
    }

    @Override
    public RecordSchema getQuerySchema(RedshiftWarehouseConfig redshiftWarehouseConfig, String query) throws Exception {
        try (Connection connection = getConnection(redshiftWarehouseConfig)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                return resultSetSchemaMapper.getSchema(preparedStatement.getMetaData());
            }
        }
    }

    @Override
    public WarehouseSyncFailureListener syncFailureListener(WarehousePollContext warehousePollContext) {
        return new RedshiftSyncFailureListener(warehousePollContext);
    }

    @Override
    public TableProperties getSnapshotTableProperties(List<String> recordIdKeys) {

        if (CollectionUtils.isEmpty(recordIdKeys) || recordIdKeys.size() > 1) {
            return new RedshiftTableProperties(null, null);
        }
        return new RedshiftTableProperties(recordIdKeys, recordIdKeys.get(0));
    }

    @Override
    public void restartPoll(String pipelineUUID, RedshiftWarehouseConfig redshiftWarehouseConfig) throws Exception {
        try (Connection connection = getConnection(redshiftWarehouseConfig)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("drop table if exists %s", ConnectorExecutionConstants.getQualifiedCommittedSnapshot(pipelineUUID)));
            }
        }
    }

    @Override
    public QueryResults previewQuery(String query, RedshiftWarehouseConfig redshiftWarehouseConfig, int maxRows) throws Exception {
        try (Connection connection = getConnection(redshiftWarehouseConfig)) {
            try (Statement statement = connection.createStatement()) {
                statement.setMaxRows(maxRows);
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    return JDBCUtils.getQueryResults(resultSet);
                }
            }
        }
    }

    @Override
    public JdbcConnectionType getConnectionType() {
        return JdbcConnectionType.REDSHIFT;
    }

    @Override
    public Class<RedshiftWarehouseConfig> getConfigType() {
        return RedshiftWarehouseConfig.class;
    }
}
