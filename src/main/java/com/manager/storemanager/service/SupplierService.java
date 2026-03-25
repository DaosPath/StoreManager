package com.manager.storemanager.service;

import com.manager.storemanager.dao.SupplierDao;
import com.manager.storemanager.model.Supplier;
import com.manager.storemanager.util.ValidationUtils;
import java.sql.SQLException;
import java.util.List;

public class SupplierService {

    private final SupplierDao supplierDao = new SupplierDao();

    public List<Supplier> findSuppliers(String search) throws SQLException {
        return supplierDao.findAll(search);
    }

    public Supplier save(Supplier supplier) throws SQLException {
        validate(supplier);
        return supplierDao.save(supplier);
    }

    public void update(Supplier supplier) throws SQLException {
        validate(supplier);
        supplierDao.update(supplier);
    }

    private void validate(Supplier supplier) {
        ValidationUtils.requireNotBlank(supplier.getName(), "nombre");
    }
}
