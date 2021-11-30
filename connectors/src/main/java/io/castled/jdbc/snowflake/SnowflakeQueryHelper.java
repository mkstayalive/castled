package io.castled.jdbc.snowflake;

import com.google.inject.Singleton;
import io.castled.jdbc.JdbcConnectionType;
import io.castled.jdbc.JdbcQueryHelper;
import io.castled.utils.StringUtils;

import java.util.Properties;

@Singleton
public class SnowflakeQueryHelper implements JdbcQueryHelper {
    @Override
    public String constructJdbcUrl(String server, int port, String database) {
        return String.format("jdbc:%s://%s:%d", JdbcConnectionType.SNOWFLAKE.getName(), server, port);
    }

    public Properties getConnectionProperties(String warehouse, String database, String schema) {
        Properties properties = new Properties();
        properties.put("warehouse", warehouse);
        properties.put("db", StringUtils.quoteText(database));
        properties.put("schema", StringUtils.quoteText(database));
        return properties;
    }
}
