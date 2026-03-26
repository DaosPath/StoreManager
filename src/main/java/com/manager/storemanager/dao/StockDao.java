package com.manager.storemanager.dao;

import com.manager.storemanager.config.DatabaseConnection;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.StockEntryItem;
import com.manager.storemanager.model.StockEntryRequest;
import com.manager.storemanager.model.StockMovement;
import com.manager.storemanager.model.User;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
                       m.referencia_tipo,
                       m.referencia_id, m.fecha_movimiento, m.costo_unitario, m.subtotal_movimiento, m.lote,
                       m.fecha_vencimiento,
                       p.id AS producto_id, p.codigo, p.nombre,
                       u.id AS usuario_id, u.nombre_completo,
                       d.tipo_registro, d.tipo_documento, d.serie, d.correlativo, d.almacen_destino,
                       d.tipo_ajuste, d.motivo_general, d.observacion,
                       pr.id AS proveedor_id, pr.nombre AS proveedor_nombre
                FROM movimientos_inventario m
                INNER JOIN productos p ON p.id = m.producto_id
                INNER JOIN usuarios u ON u.id = m.usuario_id
                LEFT JOIN documentos_stock d ON d.id = m.referencia_id
                LEFT JOIN proveedores pr ON pr.id = d.proveedor_id
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
                movement.setReferenceType(resultSet.getString("referencia_tipo"));
                long referenceId = resultSet.getLong("referencia_id");
                movement.setReferenceId(resultSet.wasNull() ? null : referenceId);
                movement.setEntryMode(resultSet.getString("tipo_registro"));
                movement.setDocumentType(resultSet.getString("tipo_documento"));
                movement.setDocumentSeries(resultSet.getString("serie"));
                movement.setDocumentCorrelative(resultSet.getString("correlativo"));
                movement.setDocumentReference(buildReference(resultSet.getString("serie"), resultSet.getString("correlativo")));
                movement.setDestination(resultSet.getString("almacen_destino"));
                movement.setAdjustmentType(resultSet.getString("tipo_ajuste"));
                movement.setGeneralReason(resultSet.getString("motivo_general"));
                movement.setGeneralNote(resultSet.getString("observacion"));
                long supplierId = resultSet.getLong("proveedor_id");
                movement.setSupplierId(resultSet.wasNull() ? null : supplierId);
                movement.setSupplierName(resultSet.getString("proveedor_nombre"));
                movement.setLot(resultSet.getString("lote"));
                Date expiration = resultSet.getDate("fecha_vencimiento");
                movement.setExpirationDate(expiration == null ? null : expiration.toLocalDate());
                movement.setUnitCost(resultSet.getBigDecimal("costo_unitario"));
                movement.setMovementSubtotal(resultSet.getBigDecimal("subtotal_movimiento"));
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
                (producto_id, usuario_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo, motivo, referencia_tipo, referencia_id)
                VALUES (?, ?, 'ENTRADA', ?, ?, ?, ?, ?, ?)
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
                        throw new SQLException("No se encontro el producto seleccionado.");
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
                movementStatement.setNull(7, Types.VARCHAR);
                movementStatement.setNull(8, Types.BIGINT);
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

    public void registerStockEntry(StockEntryRequest request) throws SQLException {
        String insertDocumentSql = """
                INSERT INTO documentos_stock
                (tipo_registro, proveedor_id, usuario_id, tipo_documento, serie, correlativo, almacen_destino,
                 tipo_ajuste, motivo_general, observacion, total_lineas, total_unidades, monto_total)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        String lockSql = "SELECT stock FROM productos WHERE id = ? FOR UPDATE";
        String updateProductSql = """
                UPDATE productos
                SET stock = ?, precio_compra = COALESCE(?, precio_compra)
                WHERE id = ?
                """;
        String movementSql = """
                INSERT INTO movimientos_inventario
                (producto_id, usuario_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo, costo_unitario,
                 subtotal_movimiento, lote, fecha_vencimiento, motivo, referencia_tipo, referencia_id)
                VALUES (?, ?, 'ENTRADA', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement documentStatement = connection.prepareStatement(insertDocumentSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement lockStatement = connection.prepareStatement(lockSql);
                 PreparedStatement updateProductStatement = connection.prepareStatement(updateProductSql);
                 PreparedStatement movementStatement = connection.prepareStatement(movementSql)) {

                long documentId = insertDocument(documentStatement, request);

                for (StockEntryItem item : request.getItems()) {
                    Product product = item.getProduct();
                    int currentStock = findCurrentStock(lockStatement, product.getId());
                    int newStock = currentStock + item.getQuantity();
                    BigDecimal purchasePrice = item.getUnitCost() != null && item.getUnitCost().compareTo(BigDecimal.ZERO) > 0
                            ? item.getUnitCost()
                            : null;

                    updateProduct(product.getId(), newStock, purchasePrice, updateProductStatement);
                    insertMovement(movementStatement, request, item, currentStock, newStock, documentId);
                }

                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private long insertDocument(PreparedStatement statement, StockEntryRequest request) throws SQLException {
        statement.setString(1, request.getEntryMode());
        if (request.getSupplier() == null || request.getSupplier().getId() == null) {
            statement.setNull(2, Types.BIGINT);
        } else {
            statement.setLong(2, request.getSupplier().getId());
        }
        statement.setLong(3, request.getUser().getId());
        setNullableString(statement, 4, request.getDocumentType());
        setNullableString(statement, 5, request.getSeries());
        setNullableString(statement, 6, request.getCorrelative());
        statement.setString(7, request.getDestination());
        setNullableString(statement, 8, request.getAdjustmentType());
        setNullableString(statement, 9, request.getGeneralReason());
        setNullableString(statement, 10, request.getNote());
        statement.setInt(11, request.getTotalLines());
        statement.setInt(12, request.getTotalUnits());
        statement.setBigDecimal(13, request.getTotalAmount());
        statement.executeUpdate();

        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getLong(1);
            }
        }
        throw new SQLException("No fue posible crear el documento de stock.");
    }

    private int findCurrentStock(PreparedStatement statement, Long productId) throws SQLException {
        statement.setLong(1, productId);
        try (ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new SQLException("No se encontro el producto seleccionado.");
            }
            return resultSet.getInt("stock");
        }
    }

    private void updateProduct(Long productId, int newStock, BigDecimal purchasePrice, PreparedStatement statement) throws SQLException {
        statement.setInt(1, newStock);
        if (purchasePrice == null) {
            statement.setNull(2, Types.DECIMAL);
        } else {
            statement.setBigDecimal(2, purchasePrice);
        }
        statement.setLong(3, productId);
        statement.executeUpdate();
    }

    private void insertMovement(PreparedStatement statement,
                                StockEntryRequest request,
                                StockEntryItem item,
                                int currentStock,
                                int newStock,
                                long documentId) throws SQLException {
        statement.setLong(1, item.getProduct().getId());
        statement.setLong(2, request.getUser().getId());
        statement.setInt(3, item.getQuantity());
        statement.setInt(4, currentStock);
        statement.setInt(5, newStock);
        setNullableBigDecimal(statement, 6, item.getUnitCost());
        setNullableBigDecimal(statement, 7, item.getSubtotal());
        setNullableString(statement, 8, item.getLot());
        setNullableDate(statement, 9, item.getExpirationDate());
        statement.setString(10, buildMovementReason(request));
        statement.setString(11, "DOCUMENTO_STOCK");
        statement.setLong(12, documentId);
        statement.executeUpdate();
    }

    private String buildMovementReason(StockEntryRequest request) {
        if ("COMPRA".equalsIgnoreCase(request.getEntryMode())) {
            String documentType = safeText(request.getDocumentType(), "FACTURA");
            String reference = safeText(request.getDocumentReference(), "").trim();
            return reference.isBlank()
                    ? "Compra " + documentType
                    : "Compra " + documentType + " " + reference;
        }
        String adjustmentType = safeText(request.getAdjustmentType(), "Ajuste manual");
        String generalReason = safeText(request.getGeneralReason(), "").trim();
        return generalReason.isBlank()
                ? "Ajuste " + adjustmentType
                : "Ajuste " + adjustmentType + " - " + generalReason;
    }

    private String buildReference(String series, String correlative) {
        String left = safeText(series, "").trim();
        String right = safeText(correlative, "").trim();
        if (left.isBlank() && right.isBlank()) {
            return null;
        }
        if (left.isBlank()) {
            return right;
        }
        if (right.isBlank()) {
            return left;
        }
        return left + "-" + right;
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value.trim());
        }
    }

    private void setNullableBigDecimal(PreparedStatement statement, int index, BigDecimal value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DECIMAL);
        } else {
            statement.setBigDecimal(index, value);
        }
    }

    private void setNullableDate(PreparedStatement statement, int index, java.time.LocalDate value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, Date.valueOf(value));
        }
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
