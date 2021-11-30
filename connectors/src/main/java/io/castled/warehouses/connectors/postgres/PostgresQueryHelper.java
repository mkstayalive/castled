package io.castled.warehouses.connectors.postgres;

import io.castled.jdbc.JdbcConnectionType;
import io.castled.jdbc.JdbcQueryHelper;

public class PostgresQueryHelper implements JdbcQueryHelper {
    @Override
    public String constructJdbcUrl(String server, int port, String database) {
        return String.format("jdbc:%s://%s:%d/%s", JdbcConnectionType.POSTGRES.getName(), server, port, database);
    }
}
