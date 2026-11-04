package com.petfinder.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Central connection factory. Credentials come from environment variables, not Git. */
public final class Database {
    private Database() { }

    public static Connection connect() throws SQLException {
        String host = env("DB_HOST", "localhost");
        String port = env("DB_PORT", "3306");
        String name = env("DB_NAME", "pet_finder");
        String user = env("DB_USER", "petfinder_app");
        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                "DB_PASSWORD is missing. Export it in your terminal before running Phase3Demo."
            );
        }
        if (!port.matches("[0-9]{1,5}")) {
            throw new IllegalArgumentException("DB_PORT must be numeric");
        }

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?connectionTimeZone=UTC";
        return DriverManager.getConnection(jdbcUrl, user, password);
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
