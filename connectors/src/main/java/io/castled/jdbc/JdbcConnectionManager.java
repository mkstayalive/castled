package io.castled.jdbc;

import com.jcraft.jsch.JSchException;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.tunnel.HostAndPort;
import io.castled.tunnel.SSHSessionRef;
import io.castled.tunnel.SSHTunnelManager;
import io.castled.tunnel.SSHTunnelParams;
import io.castled.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

@Singleton
@Slf4j
public class JdbcConnectionManager {

    private static final String DB_USER_PROPERTY = "user";
    private static final String DB_PASSWORD_PROPERTY = "password";
    private final Map<JdbcConnectionType, JdbcQueryHelper> queryHelpers;
    private final SSHTunnelManager sshTunnelManager;

    @Inject
    public JdbcConnectionManager(Map<JdbcConnectionType, JdbcQueryHelper> queryHelpers,
                                 SSHTunnelManager sshTunnelManager) {
        this.queryHelpers = queryHelpers;
        this.sshTunnelManager = sshTunnelManager;
    }

    public Connection getConnection(JdbcConnectionType jdbcConnectionType, String user, String pass, String database,
                                    String schema, String server, int port) throws SQLException {
        return getTunneledConnection(jdbcConnectionType, user, pass, database, schema, server, port, null, null);
    }

    public Connection getTunneledConnection(JdbcConnectionType jdbcConnectionType, String user, String pass, String database,
                                            String schema, String server, int port, SSHTunnelParams sshTunnelParams, Properties connProperties) throws SQLException {
        try {
            JdbcQueryHelper jdbcQueryHelper = this.queryHelpers.get(jdbcConnectionType);
            SSHSessionRef sshSessionRef = SSHSessionRef.builder()
                    .remoteHost(server).remotePort(port)
                    .sshTunnelParams(sshTunnelParams)
                    .build();
            HostAndPort tunneledHostAndPort = sshTunnelManager.getTunneledHostAndPort(sshSessionRef);
            String url = jdbcQueryHelper.constructJdbcUrl(tunneledHostAndPort.getHost(), tunneledHostAndPort.getPort(), database);
            DriverManager.setLoginTimeout((int) TimeUtils.minutesToSeconds(1));
            if (connProperties == null) {
                connProperties = new Properties();
            }
            connProperties.put(DB_USER_PROPERTY, user);
            connProperties.put(DB_PASSWORD_PROPERTY, pass);
            Connection connection = DriverManager.getConnection(url, connProperties);
            connection.setSchema(schema);
            return connection;
        } catch (JSchException e) {
            log.error("Established tunneled connected failed for server {} and port {}", server, port);
            throw new CastledRuntimeException(e);
        }
    }

}

