
package com.airline.util.connection.config;

public class DatabaseConfig {

    public static final String URL =
            "jdbc:oracle:thin:@localhost:1521/orclpdb";

    public static final String USERNAME =
            "AIRLINE_APP";

    public static final String PASSWORD =
            System.getenv("AIRLINE_DB_PASSWORD");

    private DatabaseConfig() {
    }
}
