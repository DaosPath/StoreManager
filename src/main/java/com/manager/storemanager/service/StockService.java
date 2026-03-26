package com.manager.storemanager.service;

import com.manager.storemanager.dao.StockDao;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.StockMovement;
import com.manager.storemanager.model.User;
import com.manager.storemanager.util.ValidationUtils;
import java.sql.SQLException;
import java.util.List;

public class StockService {

    private final StockDao stockDao = new StockDao();

    public List<Product> findCurrentStock() throws SQLException {
        return stockDao.findCurrentStock();
    }

    public List<StockMovement> findMovements() throws SQLException {
        return stockDao.findMovements();
    }

    public void registerStockEntry(Product product, int quantity, String reason, User user) throws SQLException {
        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto.");
        }
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("No hay usuario autenticado.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        ValidationUtils.requireNotBlank(reason, "motivo");
        stockDao.registerStockEntry(product.getId(), quantity, reason.trim(), user.getId());
    }
}
