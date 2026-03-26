package com.manager.storemanager.dao;

import com.manager.storemanager.config.DatabaseConnection;
import com.manager.storemanager.model.Customer;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.Sale;
import com.manager.storemanager.model.SaleDetail;
import com.manager.storemanager.model.User;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaleDao {

    public Long createSale(Sale sale) throws SQLException {
        String saleSql = """
                INSERT INTO ventas (usuario_id, cliente_id, fecha_venta, subtotal, impuesto, total, metodo_pago, estado, observacion)
                VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?)
                """;
        String detailSql = """
                INSERT INTO detalle_ventas
                (venta_id, producto_id, cantidad, precio_unitario, impuesto_unitario, subtotal_linea, total_linea)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        String lockProductSql = "SELECT nombre, stock FROM productos WHERE id = ? FOR UPDATE";
        String updateStockSql = "UPDATE productos SET stock = ? WHERE id = ?";
        String movementSql = """
                INSERT INTO movimientos_inventario
                (producto_id, usuario_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo, motivo, referencia_tipo, referencia_id)
                VALUES (?, ?, 'SALIDA', ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement saleStatement = connection.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement detailStatement = connection.prepareStatement(detailSql);
                 PreparedStatement lockStatement = connection.prepareStatement(lockProductSql);
                 PreparedStatement updateStockStatement = connection.prepareStatement(updateStockSql);
                 PreparedStatement movementStatement = connection.prepareStatement(movementSql)) {

                saleStatement.setLong(1, sale.getUser().getId());
                if (sale.getCustomer() != null && sale.getCustomer().getId() != null) {
                    saleStatement.setLong(2, sale.getCustomer().getId());
                } else {
                    saleStatement.setNull(2, Types.BIGINT);
                }
                saleStatement.setBigDecimal(3, sale.getSubtotal());
                saleStatement.setBigDecimal(4, sale.getTax());
                saleStatement.setBigDecimal(5, sale.getTotal());
                saleStatement.setString(6, sale.getPaymentMethod());
                saleStatement.setString(7, sale.getStatus());
                saleStatement.setString(8, sale.getObservation());
                saleStatement.executeUpdate();

                Long saleId;
                try (ResultSet keys = saleStatement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No se pudo generar la venta.");
                    }
                    saleId = keys.getLong(1);
                }

                for (SaleDetail detail : sale.getDetails()) {
                    lockStatement.setLong(1, detail.getProduct().getId());
                    String productName;
                    int currentStock;
                    try (ResultSet resultSet = lockStatement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new SQLException("El producto no existe: " + detail.getProduct().getName());
                        }
                        productName = resultSet.getString("nombre");
                        currentStock = resultSet.getInt("stock");
                    }

                    if (currentStock < detail.getQuantity()) {
                        throw new SQLException("Stock insuficiente para " + productName + ".");
                    }

                    detailStatement.setLong(1, saleId);
                    detailStatement.setLong(2, detail.getProduct().getId());
                    detailStatement.setInt(3, detail.getQuantity());
                    detailStatement.setBigDecimal(4, detail.getUnitPrice());
                    detailStatement.setBigDecimal(5, detail.getUnitTax());
                    detailStatement.setBigDecimal(6, detail.getLineSubtotal());
                    detailStatement.setBigDecimal(7, detail.getLineTotal());
                    detailStatement.executeUpdate();

                    int newStock = currentStock - detail.getQuantity();
                    updateStockStatement.setInt(1, newStock);
                    updateStockStatement.setLong(2, detail.getProduct().getId());
                    updateStockStatement.executeUpdate();

                    movementStatement.setLong(1, detail.getProduct().getId());
                    movementStatement.setLong(2, sale.getUser().getId());
                    movementStatement.setInt(3, detail.getQuantity());
                    movementStatement.setInt(4, currentStock);
                    movementStatement.setInt(5, newStock);
                    movementStatement.setString(6, "Venta #" + saleId);
                    movementStatement.setString(7, "VENTA");
                    movementStatement.setLong(8, saleId);
                    movementStatement.executeUpdate();
                }

                connection.commit();
                return saleId;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Sale> findSalesByDateRange(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
                SELECT v.id, v.fecha_venta, v.subtotal, v.impuesto, v.total, v.metodo_pago, v.estado, v.observacion,
                       u.id AS usuario_id, u.nombre_completo,
                       c.id AS cliente_id, c.nombre AS cliente_nombre
                FROM ventas v
                INNER JOIN usuarios u ON u.id = v.usuario_id
                LEFT JOIN clientes c ON c.id = v.cliente_id
                WHERE DATE(v.fecha_venta) BETWEEN ? AND ?
                ORDER BY v.fecha_venta DESC
                """;
        List<Sale> sales = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(from));
            statement.setDate(2, Date.valueOf(to));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Sale sale = new Sale();
                    sale.setId(resultSet.getLong("id"));
                    Timestamp saleDate = resultSet.getTimestamp("fecha_venta");
                    sale.setSaleDate(saleDate == null ? null : saleDate.toLocalDateTime());
                    sale.setSubtotal(resultSet.getBigDecimal("subtotal"));
                    sale.setTax(resultSet.getBigDecimal("impuesto"));
                    sale.setTotal(resultSet.getBigDecimal("total"));
                    sale.setPaymentMethod(resultSet.getString("metodo_pago"));
                    sale.setStatus(resultSet.getString("estado"));
                    sale.setObservation(resultSet.getString("observacion"));

                    User user = new User();
                    user.setId(resultSet.getLong("usuario_id"));
                    user.setFullName(resultSet.getString("nombre_completo"));
                    sale.setUser(user);

                    long customerId = resultSet.getLong("cliente_id");
                    if (!resultSet.wasNull()) {
                        Customer customer = new Customer();
                        customer.setId(customerId);
                        customer.setName(resultSet.getString("cliente_nombre"));
                        sale.setCustomer(customer);
                    }
                    sales.add(sale);
                }
            }
        }
        return sales;
    }

    public LocalDate findEarliestSaleDate() throws SQLException {
        String sql = "SELECT MIN(DATE(fecha_venta)) AS fecha_minima FROM ventas";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return null;
            }
            Date date = resultSet.getDate("fecha_minima");
            return date == null ? null : date.toLocalDate();
        }
    }
}
