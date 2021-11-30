package io.castled.jdbc;

import lombok.Getter;

public enum JdbcConnectionType {
    REDSHIFT("redshift"),
    SNOWFLAKE("snowflake"),
    POSTGRES("postgresql");

    JdbcConnectionType(String name) {
        this.name = name;
    }

    @Getter
    private String name;


}
