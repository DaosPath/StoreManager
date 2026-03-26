package com.manager.storemanager.dao;

import com.manager.storemanager.config.DatabaseConnection;
import com.manager.storemanager.model.Supplier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SupplierDao {

    public List<Supplier> findAll(String search) throws SQLException {
        String filter = search == null ? "" : search.trim();
        StringBuilder sql = new StringBuilder("""
                SELECT id, nombre, telefono, correo, direccion, activo
                FROM proveedores
                WHERE activo = 1
                """);
        if (!filter.isEmpty()) {
            sql.append(" AND (nombre LIKE ? OR telefono LIKE ? OR correo LIKE ?)");
        }
        sql.append(" ORDER BY nombre");

        List<Supplier> suppliers = new ArrayList<>();
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
                    suppliers.add(mapSupplier(resultSet));
                }
            }
        }
        return suppliers;
    }

    public Supplier save(Supplier supplier) throws SQLException {
        String sql = """
                INSERT INTO proveedores (nombre, telefono, correo, direccion, activo)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, supplier.getName());
            statement.setString(2, supplier.getPhone());
            statement.setString(3, supplier.getEmail());
            statement.setString(4, supplier.getAddress());
            statement.setBoolean(5, supplier.isActive());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    supplier.setId(keys.getLong(1));
                }
            }
            return supplier;
        }
    }

    public void update(Supplier supplier) throws SQLException {
        String sql = """
                UPDATE proveedores
                SET nombre = ?, telefono = ?, correo = ?, direccion = ?, activo = ?
                WHERE id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, supplier.getName());
            statement.setString(2, supplier.getPhone());
            statement.setString(3, supplier.getEmail());
            statement.setString(4, supplier.getAddress());
            statement.setBoolean(5, supplier.isActive());
            statement.setLong(6, supplier.getId());
            statement.executeUpdate();
        }
    }

    public void deactivate(Long supplierId) throws SQLException {
        String sql = "UPDATE proveedores SET activo = 0 WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, supplierId);
            statement.executeUpdate();
        }
    }

    private Supplier mapSupplier(ResultSet resultSet) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setId(resultSet.getLong("id"));
        supplier.setName(resultSet.getString("nombre"));
        supplier.setPhone(resultSet.getString("telefono"));
        supplier.setEmail(resultSet.getString("correo"));
        supplier.setAddress(resultSet.getString("direccion"));
        supplier.setActive(resultSet.getBoolean("activo"));
        return supplier;
    }
}
