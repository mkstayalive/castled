package io.castled.warehouses.connectors.postgres;

import com.google.common.collect.Lists;
import io.castled.ObjectRegistry;
import io.castled.constants.ConnectorExecutionConstants;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.models.Field;
import io.castled.schema.models.SchemaType;
import io.castled.schema.models.Tuple;
import io.castled.utils.StringUtils;
import io.castled.warehouses.WarehouseConfig;
import io.castled.warehouses.WarehouseSyncFailureListener;
import io.castled.warehouses.models.WarehousePollContext;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PostgresSyncFailureListener extends WarehouseSyncFailureListener {

    private static final int MAX_BUFFERED_RECORDS = 100;
    private final List<Tuple> bufferedRecords = Lists.newArrayList();
    private final Connection connection;
    private long failedRecords = 0;
    private boolean failedRecordsTableCreated = false;

    public PostgresSyncFailureListener(WarehousePollContext warehousePollContext) throws Exception {
        super(warehousePollContext);
        this.connection = ObjectRegistry.getInstance(PostgresWarehouseConnector.class).getConnection((PostgresWarehouseConfig) warehousePollContext.getWarehouseConfig());
    }

    @Override
    public synchronized void doWriteRecord(Tuple record) throws Exception {
        this.bufferedRecords.add(record);
        this.failedRecords++;
        if (bufferedRecords.size() >= MAX_BUFFERED_RECORDS) {
            insertBufferedRecords(bufferedRecords);
            this.bufferedRecords.clear();
        }
    }

    private void commitSnapshot() throws SQLException {
        connection.setAutoCommit(false);
        boolean autoCommit = connection.getAutoCommit();
        if (autoCommit) {
            connection.setAutoCommit(false);
        }
        String uncommittedSnapshot = ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID());
        String committedSnapshot = ConnectorExecutionConstants.getQualifiedCommittedSnapshot(warehousePollContext.getPipelineUUID());
        try {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("drop table if exists %s", committedSnapshot));
                statement.execute(String.format("alter table %s rename to %s", uncommittedSnapshot,
                        ConnectorExecutionConstants.getCommittedSnapshot(warehousePollContext.getPipelineUUID())));
            }
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            log.error("Committing snapshot for pipeline {} failed", warehousePollContext.getPipelineUUID(), e);
            throw new CastledRuntimeException(e);
        }
        if (autoCommit) {
            connection.setAutoCommit(true);
        }
    }

    private void createFailedRecordsTable() throws SQLException {

        PostgresClient postgresClient = ObjectRegistry.getInstance(PostgresClient.class);
        String failedRecordsCreateQuery = String.format("select %s from %s limit 0",
                String.join(",", trackableFields), ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID()));
        String tableName = ConnectorExecutionConstants.getFailedRecordsTable(warehousePollContext.getPipelineUUID());
        PostgresTableProperties postgresTableProperties = (PostgresTableProperties)
                ObjectRegistry.getInstance(PostgresWarehouseConnector.class).getSnapshotTableProperties(warehousePollContext.getPrimaryKeys());
        postgresClient.createTableFromQuery(connection, tableName, failedRecordsCreateQuery, true, postgresTableProperties);
    }

    @Override
    public void cleanupResources(String pipelineUUID, Long pipelineRunId, WarehouseConfig warehouseConfig) {

    }

    @Override
    public void doFlush() throws Exception {
        if (bufferedRecords.size() > 0) {
            insertBufferedRecords(bufferedRecords);
        }
        if (failedRecords > 0) {
            removeFailedRecordsFromSnapshot();
        }
        commitSnapshot();
        this.connection.close();

    }

    private void insertBufferedRecords(List<Tuple> bufferedRecords) throws SQLException {
        if (!failedRecordsTableCreated) {
            createFailedRecordsTable();
            this.failedRecordsTableCreated = true;
        }
        String failedRecordsTable = ConnectorExecutionConstants.getFailedRecordsTable(warehousePollContext.getPipelineUUID());

        try (Statement statement = connection.createStatement()) {
            for (Tuple record : bufferedRecords) {
                String insertRecordQuery = String.format("insert into %s(", failedRecordsTable) + record.getFields().stream().map(Field::getName).collect(Collectors.joining(",")) +
                        ") values(" +
                        record.getFields().stream().map(this::getQueryValue).collect(Collectors.joining(",")) + ")";
                statement.execute(insertRecordQuery);
            }
            statement.executeBatch();
        }
    }

    private String getQueryValue(Field field) {
        if (field.getValue() == null) {
            return "null";
        }
        if (field.getSchema().getType().equals(SchemaType.STRING)) {
            return StringUtils.singleQuote((String) field.getValue());
        }
        return field.getValue().toString();
    }

    private void removeFailedRecordsFromSnapshot() throws SQLException {

        String uncommittedSnapshot = ConnectorExecutionConstants.getQualifiedUncommittedSnapshot(warehousePollContext.getPipelineUUID());
        String failedRecordsTable = ConnectorExecutionConstants.getFailedRecordsTable(warehousePollContext.getPipelineUUID());
        StringBuilder failedRecordsDeleteQuery = new StringBuilder(String.format("delete from %s using %s where 1 = 1", uncommittedSnapshot, failedRecordsTable));
        for (String trackableField : trackableFields) {
            failedRecordsDeleteQuery.append(String.format(" AND (%s.%s = %s.%s)",
                    failedRecordsTable, trackableField, uncommittedSnapshot, trackableField));
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(failedRecordsDeleteQuery.toString());
        }
    }
}
