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
                PROPERTIES.getProperty("db.url"),
                PROPERTIES.getProperty("db.user"),
                PROPERTIES.getProperty("db.password")
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
}
