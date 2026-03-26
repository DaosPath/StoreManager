package com.manager.storemanager.service;

import com.manager.storemanager.dao.StockDao;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.StockEntryItem;
import com.manager.storemanager.model.StockEntryRequest;
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

    public void registerStockEntry(StockEntryRequest request) throws SQLException {
        if (request == null) {
            throw new IllegalArgumentException("No hay movimiento para registrar.");
        }
        User user = request.getUser();
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("No hay usuario autenticado.");
        }
        ValidationUtils.requireNotBlank(request.getEntryMode(), "modo de movimiento");
        ValidationUtils.requireNotBlank(request.getDestination(), "almacen destino");
        if (request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Debes agregar al menos una fila.");
        }

        boolean purchase = "COMPRA".equalsIgnoreCase(request.getEntryMode());
        boolean adjustment = "AJUSTE".equalsIgnoreCase(request.getEntryMode());
        if (!purchase && !adjustment) {
            throw new IllegalArgumentException("El modo de movimiento no es valido.");
        }

        if (purchase) {
            if (request.getSupplier() == null || request.getSupplier().getId() == null) {
                throw new IllegalArgumentException("Debes seleccionar un proveedor.");
            }
            ValidationUtils.requireNotBlank(request.getDocumentType(), "tipo comprobante");
            ValidationUtils.requireNotBlank(request.getSeries(), "serie");
            ValidationUtils.requireNotBlank(request.getCorrelative(), "correlativo");
        } else {
            ValidationUtils.requireNotBlank(request.getAdjustmentType(), "tipo de ajuste");
            ValidationUtils.requireNotBlank(request.getGeneralReason(), "motivo general");
        }

        for (StockEntryItem item : request.getItems()) {
            if (item == null || item.getProduct() == null || item.getProduct().getId() == null) {
                throw new IllegalArgumentException("Cada fila debe tener un producto valido.");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
            }
            ValidationUtils.requireNonNegative(item.getUnitCost(), "costo unitario");
        }

        stockDao.registerStockEntry(request);
    }
}
