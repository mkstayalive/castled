package io.castled.warehouses.connectors.postgres;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Singleton
public class PostgresClient {

    private static final String WHITESPACE = " ";

    public List<String> listTables(Connection connection, String schema) throws SQLException {
        String listTablesQuery = String.format("select tablename from pg_tables where schemaname = '%s'", schema);
        List<String> schemaTables = Lists.newArrayList();
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(listTablesQuery)) {
                while (resultSet.next()) {
                    schemaTables.add(resultSet.getString("tablename"));
                }
            }
        }
        return schemaTables;
    }

    public List<String> listSchemas(Connection connection) throws SQLException {
        List<String> schemas = Lists.newArrayList();
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT schema_name FROM information_schema.schemata")) {
                while (resultSet.next()) {
                    schemas.add(resultSet.getString("schema_name"));
                }
            }
        }
        return schemas;
    }


    public void createTableFromQuery(Connection connection, String tableName, String query, boolean temporary,
                                     PostgresTableProperties postgresTableProperties) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        if (autoCommit) {
            connection.setAutoCommit(false);
        }
        StringBuilder queryBuilder = new StringBuilder();
        if (temporary) {
            queryBuilder.append("CREATE TEMPORARY TABLE ").append(tableName).append(WHITESPACE);
        } else {
            queryBuilder.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(WHITESPACE);
        }

        queryBuilder.append(WHITESPACE).append("as ").append(query);
        try (Statement statement = connection.createStatement()) {
            statement.execute(queryBuilder.toString());
            statement.execute(String.format("ALTER TABLE %s ADD PRIMARY KEY(%s)", tableName,
                    String.join(",", postgresTableProperties.getPrimaryKeys())));
        }
        connection.commit();
        if (autoCommit) {
            connection.setAutoCommit(true);
        }
    }
}
