package io.castled.warehouses.connectors.redshift;

import com.google.common.collect.Lists;
import io.castled.exceptions.CastledRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Singleton
public class RedshiftClient {

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
            try (ResultSet resultSet = statement.executeQuery("select s.nspname as table_schema," +
                    " s.oid as schema_id, u.usename as owner " +
                    "from pg_catalog.pg_namespace s " +
                    "join pg_catalog.pg_user u on u.usesysid = s.nspowner "+
                    "order by table_schema;")) {
                while (resultSet.next()) {
                    schemas.add(resultSet.getString("table_schema"));
                }
            }
        }
        return schemas;
    }

    public void createTableFromQuery(Connection connection, String tableName, String query,
                                     RedshiftTableProperties tableProperties, boolean temporary) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder();
        if (temporary) {
            queryBuilder.append("CREATE TEMPORARY TABLE ").append(tableName).append(WHITESPACE);

        } else {
            queryBuilder.append("CREATE TABLE ").append(tableName).append(WHITESPACE);
        }

        if (CollectionUtils.isNotEmpty(tableProperties.getSortKeys())) {
            queryBuilder.append(WHITESPACE).append("SORTKEY(").append(String.join(",", tableProperties.getSortKeys())).append(')');

        }
        if (tableProperties.getDistributionKey() != null) {
            queryBuilder.append(WHITESPACE).append("DISTKEY(").append(tableProperties.getDistributionKey()).append(')');
        }
        queryBuilder.append(WHITESPACE).append("as ").append(query);
        try (Statement statement = connection.createStatement()) {
            statement.execute(queryBuilder.toString());

        }
    }

    public void copyFilesToTable(Connection connection, String table, String manifestUrl, String encryptionKey,
                                 RedshiftWarehouseConfig redshiftWarehouseConfig) {
        try {

            String credentials = String.format("credentials 'aws_access_key_id=%s;aws_secret_access_key=%s;master_symmetric_key=%s' ENCRYPTED",
                    redshiftWarehouseConfig.getAccessKeyId(),
                    redshiftWarehouseConfig.getAccessKeySecret(), encryptionKey);


            String query = String.format("copy  %s from '%s' manifest gzip %s json 'auto' TIMEFORMAT 'epochmillisecs' DATEFORMAT 'YYYY-MM-DD' region '%s'" +
                    " truncatecolumns COMPUPDATE OFF STATUPDATE OFF ACCEPTINVCHARS", table, manifestUrl, credentials, redshiftWarehouseConfig.getRegion().getName());

            try (Statement statement = connection.createStatement()) {
                statement.execute(query);
            }
        } catch (SQLException e) {
            log.error("Failed to copy records into table {}", table, e);
            throw new CastledRuntimeException(e);
        }
    }
}
