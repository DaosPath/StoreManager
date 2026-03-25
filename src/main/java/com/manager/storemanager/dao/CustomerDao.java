package com.manager.storemanager.dao;

import com.manager.storemanager.config.DatabaseConnection;
import com.manager.storemanager.model.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    public List<Customer> findAll(String search) throws SQLException {
        String filter = search == null ? "" : search.trim();
        StringBuilder sql = new StringBuilder("""
                SELECT id, nombre, telefono, documento, direccion, activo
                FROM clientes
                WHERE activo = 1
                """);
        if (!filter.isEmpty()) {
            sql.append(" AND (nombre LIKE ? OR documento LIKE ? OR telefono LIKE ?)");
        }
        sql.append(" ORDER BY nombre");

        List<Customer> customers = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            if (!filter.isEmpty()) {
                String likeValue = "%" + filter + "%";
                statement.setString(1, likeValue);
                statement.setString(2, likeValue);
                statement.setString(3, likeValue);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    customers.add(mapCustomer(resultSet));
                }
            }
        }
        return customers;
    }

    public Customer save(Customer customer) throws SQLException {
        String sql = """
                INSERT INTO clientes (nombre, telefono, documento, direccion, activo)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getName());
            statement.setString(2, customer.getPhone());
            statement.setString(3, customer.getDocument());
            statement.setString(4, customer.getAddress());
            statement.setBoolean(5, customer.isActive());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setId(keys.getLong(1));
                }
            }
            return customer;
        }
    }

    public void update(Customer customer) throws SQLException {
        String sql = """
                UPDATE clientes
                SET nombre = ?, telefono = ?, documento = ?, direccion = ?, activo = ?
                WHERE id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.getName());
            statement.setString(2, customer.getPhone());
            statement.setString(3, customer.getDocument());
            statement.setString(4, customer.getAddress());
            statement.setBoolean(5, customer.isActive());
            statement.setLong(6, customer.getId());
            statement.executeUpdate();
        }
    }

    private Customer mapCustomer(ResultSet resultSet) throws SQLException {
        Customer customer = new Customer();
        customer.setId(resultSet.getLong("id"));
        customer.setName(resultSet.getString("nombre"));
        customer.setPhone(resultSet.getString("telefono"));
        customer.setDocument(resultSet.getString("documento"));
        customer.setAddress(resultSet.getString("direccion"));
        customer.setActive(resultSet.getBoolean("activo"));
        return customer;
    }
}
