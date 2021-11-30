package io.castled.jdbc.redshift;

import com.google.inject.Singleton;
import io.castled.jdbc.JdbcConnectionType;
import io.castled.jdbc.JdbcQueryHelper;

@Singleton
public class RedshiftQueryHelper implements JdbcQueryHelper {
    @Override
    public String constructJdbcUrl(String server, int port, String database) {
        return String.format("jdbc:%s://%s:%d/%s", JdbcConnectionType.REDSHIFT.getName(), server, port, database);
    }
}
