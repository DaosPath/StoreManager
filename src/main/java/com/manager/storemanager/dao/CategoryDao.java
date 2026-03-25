package com.manager.storemanager.dao;

import com.manager.storemanager.config.DatabaseConnection;
import com.manager.storemanager.model.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDao {

    public List<Category> findAll() {
        String sql = "SELECT id, nombre, descripcion, activo FROM categorias WHERE activo = 1 ORDER BY nombre";
        List<Category> categories = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(map(resultSet));
            }
            return categories;
        } catch (SQLException exception) {
            throw new IllegalStateException("No fue posible consultar las categorias.", exception);
        }
    }

    public Optional<Category> findByName(String name) {
        String sql = "SELECT id, nombre, descripcion, activo FROM categorias WHERE LOWER(nombre) = LOWER(?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No fue posible consultar la categoria.", exception);
        }
    }

    public long findOrCreate(String name) {
        Optional<Category> existing = findByName(name);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        String sql = "INSERT INTO categorias (nombre, descripcion, activo) VALUES (?, ?, 1)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, "Categoria creada desde productos");
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new IllegalStateException("No fue posible generar la categoria.");
        } catch (SQLException exception) {
            throw new IllegalStateException("No fue posible registrar la categoria.", exception);
        }
    }

    private Category map(ResultSet resultSet) throws SQLException {
        Category category = new Category();
        category.setId(resultSet.getLong("id"));
        category.setName(resultSet.getString("nombre"));
        category.setDescription(resultSet.getString("descripcion"));
        category.setActive(resultSet.getBoolean("activo"));
        return category;
    }
}
