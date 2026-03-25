package com.manager.storemanager.service;

import com.manager.storemanager.dao.InventoryDao;
import com.manager.storemanager.model.InventoryMovement;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.User;
import com.manager.storemanager.util.ValidationUtils;
import java.sql.SQLException;
import java.util.List;

public class InventoryService {

    private final InventoryDao inventoryDao = new InventoryDao();

    public List<Product> findCurrentStock() throws SQLException {
        return inventoryDao.findCurrentStock();
    }

    public List<InventoryMovement> findMovements() throws SQLException {
        return inventoryDao.findMovements();
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
        inventoryDao.registerStockEntry(product.getId(), quantity, reason.trim(), user.getId());
    }
}
