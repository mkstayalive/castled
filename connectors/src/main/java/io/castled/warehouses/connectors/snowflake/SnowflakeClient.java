package io.castled.warehouses.connectors.snowflake;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Singleton
@Slf4j
public class SnowflakeClient {

    private static final String WHITESPACE = " ";

    public List<String> listTables(Connection connection, String schema) throws SQLException {
        String listTablesQuery = String.format("select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA = '%s'", schema);
        List<String> schemaTables = Lists.newArrayList();
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(listTablesQuery)) {
                while (resultSet.next()) {
                    schemaTables.add(resultSet.getString("TABLE_NAME"));
                }
            }
        }
        return schemaTables;
    }

    public List<String> listSchemas(Connection connection) throws SQLException {
        List<String> schemas = Lists.newArrayList();
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("show schemas")) {
                while (resultSet.next()) {
                    schemas.add(resultSet.getString("name"));
                }
            }
        }
        return schemas;
    }

    public void createTableFromQuery(Connection connection, String tableName, String query, boolean temporary) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder();
        if (temporary) {
            queryBuilder.append("CREATE TEMPORARY TABLE ").append(tableName).append(WHITESPACE);

        } else {
            queryBuilder.append("CREATE TABLE ").append(tableName).append(WHITESPACE);
        }
        queryBuilder.append("as ").append(query);
        try (Statement statement = connection.createStatement()) {
            statement.execute(queryBuilder.toString());
        }
    }

    public void copyFilesToTable(Connection connection, String tableName, String s3Location, String encryptionKey,
                                 String accessKeyId, String accessKeySecret) throws SQLException {
        String copyQuery = String.format("COPY INTO %s FROM '%s' " +
                        "FILE_FORMAT = (TYPE = 'CSV' COMPRESSION = 'GZIP' SKIP_HEADER = 1 FIELD_OPTIONALLY_ENCLOSED_BY = '\"') " +
                        "CREDENTIALS = (AWS_KEY_ID = '%s' AWS_SECRET_KEY = '%s') " +
                        "ENCRYPTION = (TYPE = 'AWS_CSE'  MASTER_KEY = '%s' )", tableName, s3Location,
                accessKeyId, accessKeySecret, encryptionKey);
        try (Statement statement = connection.createStatement()) {
            statement.execute(copyQuery);
        }
    }
}
