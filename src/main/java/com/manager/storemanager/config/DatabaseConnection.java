package com.manager.storemanager.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class DatabaseConnection {

    private static final Properties PROPERTIES = new Properties();
    private static final String ENV_DB_URL = "DB_URL";
    private static final String ENV_DB_USER = "DB_USER";
    private static final String ENV_DB_PASSWORD = "DB_PASSWORD";

    static {
        try (InputStream inputStream = DatabaseConnection.class.getResourceAsStream(AppConfig.DATABASE_PROPERTIES)) {
            if (inputStream == null) {
                throw new IllegalStateException("No se encontro el archivo db.properties.");
            }
            PROPERTIES.load(inputStream);
            Class.forName(PROPERTIES.getProperty("db.driver"));
        } catch (IOException | ClassNotFoundException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(
                resolve("db.url", ENV_DB_URL),
                resolve("db.user", ENV_DB_USER),
                resolve("db.password", ENV_DB_PASSWORD)
        );
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET time_zone = '-05:00'");
        }
        return connection;
    }

    public static boolean isAvailable() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (SQLException exception) {
            return false;
        }
    }

    private static String resolve(String propertyKey, String envKey) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        return PROPERTIES.getProperty(propertyKey);
    }
}
