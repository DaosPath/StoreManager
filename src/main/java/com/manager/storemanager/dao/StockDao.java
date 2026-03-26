package com.manager.storemanager.dao;

import com.manager.storemanager.config.DatabaseConnection;
import com.manager.storemanager.model.StockMovement;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class StockDao {

    public List<Product> findCurrentStock() throws SQLException {
        String sql = """
                SELECT p.id, p.codigo, p.nombre, p.stock, p.stock_minimo, p.estado,
                       c.id AS categoria_id, c.nombre AS categoria_nombre
                FROM productos p
                INNER JOIN categorias c ON c.id = p.categoria_id
                ORDER BY p.nombre
                """;
        List<Product> products = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Product product = new Product();
                product.setId(resultSet.getLong("id"));
                product.setCode(resultSet.getString("codigo"));
                product.setName(resultSet.getString("nombre"));
                product.setStock(resultSet.getInt("stock"));
                product.setMinimumStock(resultSet.getInt("stock_minimo"));
                product.setStatus(resultSet.getString("estado"));
                com.manager.storemanager.model.Category category = new com.manager.storemanager.model.Category();
                category.setId(resultSet.getLong("categoria_id"));
                category.setName(resultSet.getString("categoria_nombre"));
                product.setCategory(category);
                products.add(product);
            }
        }
        return products;
    }

    public List<StockMovement> findMovements() throws SQLException {
        String sql = """
                SELECT m.id, m.tipo_movimiento, m.cantidad, m.stock_anterior, m.stock_nuevo, m.motivo,
                       m.referencia_id, m.fecha_movimiento,
                       p.id AS producto_id, p.codigo, p.nombre,
                       u.id AS usuario_id, u.nombre_completo
                FROM movimientos_inventario m
                INNER JOIN productos p ON p.id = m.producto_id
                INNER JOIN usuarios u ON u.id = m.usuario_id
                ORDER BY m.fecha_movimiento DESC
                LIMIT 200
                """;
        List<StockMovement> movements = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Product product = new Product();
                product.setId(resultSet.getLong("producto_id"));
                product.setCode(resultSet.getString("codigo"));
                product.setName(resultSet.getString("nombre"));

                User user = new User();
                user.setId(resultSet.getLong("usuario_id"));
                user.setFullName(resultSet.getString("nombre_completo"));

                StockMovement movement = new StockMovement();
                movement.setId(resultSet.getLong("id"));
                movement.setMovementType(resultSet.getString("tipo_movimiento"));
                movement.setQuantity(resultSet.getInt("cantidad"));
                movement.setPreviousStock(resultSet.getInt("stock_anterior"));
                movement.setNewStock(resultSet.getInt("stock_nuevo"));
                movement.setReason(resultSet.getString("motivo"));
                long referenceId = resultSet.getLong("referencia_id");
                movement.setReferenceId(resultSet.wasNull() ? null : referenceId);
                Timestamp movementDate = resultSet.getTimestamp("fecha_movimiento");
                movement.setMovementDate(movementDate == null ? null : movementDate.toLocalDateTime());
                movement.setProduct(product);
                movement.setUser(user);
                movements.add(movement);
            }
        }
        return movements;
    }

    public void registerStockEntry(Long productId, int quantity, String reason, Long userId) throws SQLException {
        String lockSql = "SELECT stock FROM productos WHERE id = ? FOR UPDATE";
        String updateSql = "UPDATE productos SET stock = stock + ? WHERE id = ?";
        String movementSql = """
                INSERT INTO movimientos_inventario
                (producto_id, usuario_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo, motivo, referencia_id)
                VALUES (?, ?, 'ENTRADA', ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement lockStatement = connection.prepareStatement(lockSql);
                 PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                 PreparedStatement movementStatement = connection.prepareStatement(movementSql)) {
                lockStatement.setLong(1, productId);
                int currentStock;
                try (ResultSet resultSet = lockStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("No se encontró el producto seleccionado.");
                    }
                    currentStock = resultSet.getInt("stock");
                }

                int newStock = currentStock + quantity;
                updateStatement.setInt(1, quantity);
                updateStatement.setLong(2, productId);
                updateStatement.executeUpdate();

                movementStatement.setLong(1, productId);
                movementStatement.setLong(2, userId);
                movementStatement.setInt(3, quantity);
                movementStatement.setInt(4, currentStock);
                movementStatement.setInt(5, newStock);
                movementStatement.setString(6, reason);
                movementStatement.setNull(7, Types.BIGINT);
                movementStatement.executeUpdate();

                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
