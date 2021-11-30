package io.castled.commons.streams;

import io.castled.ObjectRegistry;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.schema.models.RecordSchema;
import io.castled.schema.models.Tuple;
import io.castled.warehouses.connectors.postgres.PostgresResultSetSchemaMapper;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class JdbcRecordInputStream implements RecordInputStream {

    private final ResultSet resultSet;
    private final Connection connection;
    private final Statement statement;
    private final RecordSchema recordSchema;

    public JdbcRecordInputStream(Connection connection, String query, RecordSchema querySchema) {
        try {
            this.connection = connection;
            this.connection.setAutoCommit(false);
            this.statement = connection.createStatement();
            this.resultSet = this.statement.executeQuery(query);
            this.recordSchema = querySchema;
        } catch (SQLException e) {
            log.error(String.format("Failed to create jdbc connection for query %s", query), e);
            throw new CastledRuntimeException(e.getMessage());
        }
    }

    @Override
    public Tuple readRecord() throws Exception {
        if (resultSet.next()) {
            return ObjectRegistry.getInstance(PostgresResultSetSchemaMapper.class).getRecord(resultSet, recordSchema);
        }
        return null;
    }

    public void close() {
        try {
            this.resultSet.close();
            this.statement.close();
            this.connection.close();
        } catch (SQLException e) {
            log.error("Failed to close connection", e);
            throw new CastledRuntimeException(e.getMessage());
        }
    }

}
