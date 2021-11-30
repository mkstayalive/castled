package io.castled.warehouses.connectors.postgres;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.commons.streams.JdbcRecordInputStream;
import io.castled.commons.streams.RecordInputStream;
import io.castled.constants.ConnectorExecutionConstants;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.models.RecordSchema;
import io.castled.warehouses.WarehouseConfig;
import io.castled.warehouses.WarehouseDataPoller;
import io.castled.warehouses.models.WarehousePollContext;
import io.castled.warehouses.models.WarehousePollResult;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Singleton
public class PostgresDataPoller implements WarehouseDataPoller {

    private final PostgresClient postgresClient;
    private final PostgresResultSetSchemaMapper resultSetSchemaMapper;
    private final PostgresWarehouseConnector postgresWarehouseConnector;

    @Inject
    public PostgresDataPoller(PostgresClient postgresClient, PostgresResultSetSchemaMapper resultSetSchemaMapper,
                              PostgresWarehouseConnector postgresWarehouseConnector) {
        this.postgresClient = postgresClient;
        this.resultSetSchemaMapper = resultSetSchemaMapper;
        this.postgresWarehouseConnector = postgresWarehouseConnector;
    }

    @Override
    public WarehousePollResult pollRecords(WarehousePollContext warehousePollContext) {
        PostgresWarehouseConfig postgresWarehouseConfig = (PostgresWarehouseConfig) warehousePollContext.getWarehouseConfig();
        try (Connection connection = postgresWarehouseConnector.getConnection(postgresWarehouseConfig)) {
            List<String> bookKeepingTables = this.postgresClient.listTables(connection, ConnectorExecutionConstants.CASTLED_CONTAINER);
            createUncommittedSnapshot(connection, warehousePollContext, bookKeepingTables);
            RecordSchema querySchema = getSchemaFromQuery(connection, String.format("select * from %s",
                    ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID())));
            return WarehousePollResult.builder()
                    .recordInputStream(createRecordStream(connection, warehousePollContext, bookKeepingTables, querySchema))
                    .warehouseSchema(querySchema).build();
        } catch (Exception e) {
            log.error("Poll records from warehouse {} failed", warehousePollContext.getWarehouseConfig().getType(), e);
            throw new CastledRuntimeException(e);
        }
    }

    @Override
    public WarehousePollResult resumePoll(WarehousePollContext warehousePollContext) {
        try (Connection connection = postgresWarehouseConnector.getConnection((PostgresWarehouseConfig) warehousePollContext.getWarehouseConfig())) {
            List<String> bookKeepingTables = this.postgresClient.listTables(connection, ConnectorExecutionConstants.CASTLED_CONTAINER);
            if (!bookKeepingTables.contains(ConnectorExecutionConstants.getUncommittedSnapshot(warehousePollContext.getPipelineUUID()))) {
                return pollRecords(warehousePollContext);

            }
            RecordSchema querySchema = getSchemaFromQuery(connection, String.format("select * from %s",
                    ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID())));
            return WarehousePollResult.builder()
                    .recordInputStream(createRecordStream(connection, warehousePollContext, bookKeepingTables, querySchema))
                    .warehouseSchema(querySchema).resumed(true).build();
        } catch (Exception e) {
            log.error("Resume poll from warehouse {} failed", warehousePollContext.getWarehouseConfig().getType(), e);
            return pollRecords(warehousePollContext);
        }

    }

    private RecordInputStream createRecordStream(Connection connection, WarehousePollContext warehousePollContext,
                                                 List<String> bookKeepingTables, RecordSchema querySchema) {
        return new JdbcRecordInputStream(connection, getDataFetchQuery(warehousePollContext, bookKeepingTables), querySchema);
    }

    private String getDataFetchQuery(WarehousePollContext warehousePollRequest, List<String> bookKeepingTables) {
        String committedSnapshot = ConnectorExecutionConstants.getQualifiedCommittedSnapshot(warehousePollRequest.getPipelineUUID());
        String uncommittedSnapshot = ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollRequest.getPipelineUUID());
        if (bookKeepingTables.contains(ConnectorExecutionConstants.getCommittedSnapshot(warehousePollRequest.getPipelineUUID()))) {
            return String.format("select * from %s except select * from %s", uncommittedSnapshot, committedSnapshot);
        }
        return String.format("select * from %s", uncommittedSnapshot);
    }

    private void createUncommittedSnapshot(Connection connection, WarehousePollContext warehousePollContext, List<String> internalTables) throws SQLException {

        if (internalTables.contains(ConnectorExecutionConstants.getUncommittedSnapshot(warehousePollContext.getPipelineUUID()))) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("drop table if exists %s",
                        ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID())));
            }
        }
        this.postgresClient.createTableFromQuery(connection, ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID()),
                warehousePollContext.getQuery(), false,
                (PostgresTableProperties) ObjectRegistry.getInstance(PostgresWarehouseConnector.class).getSnapshotTableProperties(warehousePollContext.getPrimaryKeys()));
    }

    private RecordSchema getSchemaFromQuery(Connection connection, String query) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            return resultSetSchemaMapper.getSchema(preparedStatement.getMetaData());
        }
    }

    @Override
    public void cleanupPipelineRunResources(WarehousePollContext warehousePollContext) {

    }

    @Override
    public void cleanupPipelineResources(String pipelineUUID, WarehouseConfig warehouseConfig) {
        try {
            PostgresWarehouseConnector postgresWarehouseConnector = ObjectRegistry.getInstance(PostgresWarehouseConnector.class);
            try (Connection connection = postgresWarehouseConnector.getConnection((PostgresWarehouseConfig) warehouseConfig)) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(String.format("drop table if exists %s", ConnectorExecutionConstants.getQualifiedCommittedSnapshot(pipelineUUID)));
                    statement.execute(String.format("drop table if exists %s", ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(pipelineUUID)));

                }
            }
        } catch (SQLException e) {
            log.error("Cleanup pipeline resources failed for pipeline {}", pipelineUUID);
            throw new CastledRuntimeException(e);
        }
    }
}
