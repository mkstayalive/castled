package io.castled.jdbc;

import io.castled.tunnel.SSHTunnelParams;

public class JdbcConnectionParams {
    private JdbcConnectionType jdbcConnectionType;
    private String user;
    private String pass;
    private String database;
    private String schema;
    private String server;
    private int port;
    private SSHTunnelParams sshTunnelParams;
}
