package com.manager.storemanager.dao;

import com.manager.storemanager.config.DatabaseConnection;
import com.manager.storemanager.model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDao {

    public List<Product> findAll(String filter) {
        String sql = """
                SELECT p.id, p.categoria_id, p.proveedor_id, p.codigo, p.nombre, p.descripcion, p.precio_compra,
                       p.precio_venta, p.stock, p.stock_minimo, p.estado, c.nombre AS categoria_nombre,
                       COALESCE(pr.nombre, '') AS proveedor_nombre
                FROM productos p
                INNER JOIN categorias c ON c.id = p.categoria_id
                LEFT JOIN proveedores pr ON pr.id = p.proveedor_id
                WHERE (? = '' OR LOWER(p.nombre) LIKE ? OR LOWER(p.codigo) LIKE ?)
                ORDER BY p.nombre
                """;
        List<Product> products = new ArrayList<>();
        String search = filter == null ? "" : filter.trim().toLowerCase();
        String like = "%" + search + "%";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, search);
            statement.setString(2, like);
            statement.setString(3, like);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(map(resultSet));
                }
            }
            return products;
        } catch (SQLException exception) {
            throw new IllegalStateException("No fue posible consultar los productos.", exception);
        }
    }

    public List<Product> findAvailableForSale(String filter) {
        String sql = """
                SELECT p.id, p.categoria_id, p.proveedor_id, p.codigo, p.nombre, p.descripcion, p.precio_compra,
                       p.precio_venta, p.stock, p.stock_minimo, p.estado, c.nombre AS categoria_nombre,
                       COALESCE(pr.nombre, '') AS proveedor_nombre
                FROM productos p
                INNER JOIN categorias c ON c.id = p.categoria_id
                LEFT JOIN proveedores pr ON pr.id = p.proveedor_id
                WHERE p.estado = 'ACTIVO' AND p.stock > 0
                  AND (? = '' OR LOWER(p.nombre) LIKE ? OR LOWER(p.codigo) LIKE ?)
                ORDER BY p.nombre
                """;
        List<Product> products = new ArrayList<>();
        String search = filter == null ? "" : filter.trim().toLowerCase();
        String like = "%" + search + "%";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, search);
            statement.setString(2, like);
            statement.setString(3, like);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(map(resultSet));
                }
            }
            return products;
        } catch (SQLException exception) {
            throw new IllegalStateException("No fue posible consultar productos disponibles para venta.", exception);
        }
    }

    public Optional<Product> findById(Long id) {
        String sql = """
                SELECT p.id, p.categoria_id, p.proveedor_id, p.codigo, p.nombre, p.descripcion, p.precio_compra,
                       p.precio_venta, p.stock, p.stock_minimo, p.estado, c.nombre AS categoria_nombre,
                       COALESCE(pr.nombre, '') AS proveedor_nombre
                FROM productos p
                INNER JOIN categorias c ON c.id = p.categoria_id
                LEFT JOIN proveedores pr ON pr.id = p.proveedor_id
                WHERE p.id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No fue posible consultar el producto.", exception);
        }
    }

    public long save(Product product) {
        String sql = """
                INSERT INTO productos (categoria_id, proveedor_id, codigo, nombre, descripcion, precio_compra,
                                       precio_venta, stock, stock_minimo, estado)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, product);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new IllegalStateException("No fue posible crear el producto.");
        } catch (SQLException exception) {
            throw new IllegalStateException("No fue posible guardar el producto.", exception);
        }
    }

    public void deactivate(Long id) {
        updateStatus(id, "INACTIVO");
    }

    public void updateStatus(Long id, String status) {
        String sql = "UPDATE productos SET estado = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setLong(2, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("No fue posible actualizar el estado del producto.", exception);
        }
    }

    public void update(Product product) {
        String sql = """
                UPDATE productos
                SET categoria_id = ?, proveedor_id = ?, codigo = ?, nombre = ?, descripcion = ?, precio_compra = ?,
                    precio_venta = ?, stock = ?, stock_minimo = ?, estado = ?
                WHERE id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, product);
            statement.setLong(11, product.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("No fue posible actualizar el producto.", exception);
        }
    }

    public void delete(Long id) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM productos WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("No fue posible eliminar el producto.", exception);
        }
    }

    public int countProducts() {
        return queryForInt("SELECT COUNT(*) FROM productos");
    }

    public int countLowStock() {
        return queryForInt("SELECT COUNT(*) FROM productos WHERE stock <= stock_minimo");
    }

    public List<Product> findLowStock() {
        String sql = """
                SELECT p.id, p.categoria_id, p.proveedor_id, p.codigo, p.nombre, p.descripcion, p.precio_compra,
                       p.precio_venta, p.stock, p.stock_minimo, p.estado, c.nombre AS categoria_nombre,
                       COALESCE(pr.nombre, '') AS proveedor_nombre
                FROM productos p
                INNER JOIN categorias c ON c.id = p.categoria_id
                LEFT JOIN proveedores pr ON pr.id = p.proveedor_id
                WHERE p.stock <= p.stock_minimo
                ORDER BY p.stock ASC, p.nombre
                """;
        List<Product> products = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                products.add(map(resultSet));
            }
            return products;
        } catch (SQLException exception) {
            throw new IllegalStateException("No fue posible consultar los productos con stock bajo.", exception);
        }
    }

    public Optional<Product> findByIdForUpdate(Connection connection, Long id) throws SQLException {
        String sql = """
                SELECT p.id, p.categoria_id, p.proveedor_id, p.codigo, p.nombre, p.descripcion, p.precio_compra,
                       p.precio_venta, p.stock, p.stock_minimo, p.estado, c.nombre AS categoria_nombre,
                       COALESCE(pr.nombre, '') AS proveedor_nombre
                FROM productos p
                INNER JOIN categorias c ON c.id = p.categoria_id
                LEFT JOIN proveedores pr ON pr.id = p.proveedor_id
                WHERE p.id = ?
                FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        }
    }

    public void updateStock(Connection connection, Long id, int newStock) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE productos SET stock = ? WHERE id = ?")) {
            statement.setInt(1, newStock);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    private int queryForInt(String sql) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException exception) {
            throw new IllegalStateException("No fue posible consultar el indicador.", exception);
        }
    }

    private void fillStatement(PreparedStatement statement, Product product) throws SQLException {
        statement.setLong(1, product.getCategoryId());
        if (product.getSupplierId() == null) {
            statement.setNull(2, java.sql.Types.BIGINT);
        } else {
            statement.setLong(2, product.getSupplierId());
        }
        statement.setString(3, product.getCode());
        statement.setString(4, product.getName());
        statement.setString(5, product.getDescription());
        statement.setBigDecimal(6, product.getPurchasePrice());
        statement.setBigDecimal(7, product.getSalePrice());
        statement.setInt(8, product.getStock());
        statement.setInt(9, product.getMinimumStock());
        statement.setString(10, product.getStatus());
    }

    private Product map(ResultSet resultSet) throws SQLException {
        Product product = new Product();
        product.setId(resultSet.getLong("id"));
        product.setCategoryId(resultSet.getLong("categoria_id"));
        long supplierId = resultSet.getLong("proveedor_id");
        product.setSupplierId(resultSet.wasNull() ? null : supplierId);
        product.setCode(resultSet.getString("codigo"));
        product.setName(resultSet.getString("nombre"));
        product.setDescription(resultSet.getString("descripcion"));
        product.setCategoryName(resultSet.getString("categoria_nombre"));
        product.setSupplierName(resultSet.getString("proveedor_nombre"));
        product.setPurchasePrice(resultSet.getBigDecimal("precio_compra"));
        product.setSalePrice(resultSet.getBigDecimal("precio_venta"));
        product.setStock(resultSet.getInt("stock"));
        product.setMinimumStock(resultSet.getInt("stock_minimo"));
        product.setStatus(resultSet.getString("estado"));
        return product;
    }
}
