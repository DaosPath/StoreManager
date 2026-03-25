package com.manager.storemanager.service;

import com.manager.storemanager.dao.CategoryDao;
import com.manager.storemanager.dao.ProductDao;
import com.manager.storemanager.dao.SupplierDao;
import com.manager.storemanager.model.Category;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.Supplier;
import com.manager.storemanager.util.ValidationUtils;
import java.sql.SQLException;
import java.util.List;

public class ProductService {

    private final ProductDao productDao = new ProductDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final SupplierDao supplierDao = new SupplierDao();

    public List<Product> findProducts(String search) throws SQLException {
        return productDao.findAll(search);
    }

    public List<Product> findProductsForSale(String search) throws SQLException {
        return productDao.findAvailableForSale(search);
    }

    public List<Product> findLowStockProducts() throws SQLException {
        return productDao.findLowStock();
    }

    public List<Category> findCategories() throws SQLException {
        return categoryDao.findAll();
    }

    public List<Supplier> findSuppliers() throws SQLException {
        return supplierDao.findAll("");
    }

    public Product save(Product product) throws SQLException {
        validate(product);
        product.setId(productDao.save(product));
        return product;
    }

    public void update(Product product) throws SQLException {
        validate(product);
        productDao.update(product);
    }

    public void deactivate(Long productId) throws SQLException {
        productDao.deactivate(productId);
    }

    private void validate(Product product) {
        ValidationUtils.requireNotBlank(product.getCode(), "código");
        ValidationUtils.requireNotBlank(product.getName(), "nombre");
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoría.");
        }
        ValidationUtils.requireNonNegative(product.getPurchasePrice(), "precio compra");
        ValidationUtils.requirePositive(product.getSalePrice(), "precio venta");
        ValidationUtils.requireNonNegativeInt(product.getStock(), "stock");
        ValidationUtils.requireNonNegativeInt(product.getMinimumStock(), "stock mínimo");
        ValidationUtils.requireNotBlank(product.getStatus(), "estado");
    }
}
