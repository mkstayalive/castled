package io.castled.warehouses.jdbc;

import io.castled.jdbc.JdbcConnectionType;
import io.castled.warehouses.BaseWarehouseConnector;
import io.castled.warehouses.WarehouseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class JdbcWarehouseConnector<CONFIG extends WarehouseConfig> extends BaseWarehouseConnector<CONFIG> {

    public abstract Connection getConnection(CONFIG config) throws SQLException;

    public abstract JdbcConnectionType getConnectionType();
}
