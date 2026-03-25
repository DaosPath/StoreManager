package com.manager.storemanager.service;

import com.manager.storemanager.config.AppConfig;
import com.manager.storemanager.dao.SaleDao;
import com.manager.storemanager.model.Sale;
import com.manager.storemanager.model.SaleDetail;
import com.manager.storemanager.model.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class SaleService {

    private final SaleDao saleDao = new SaleDao();

    public Sale prepareSale(User user, com.manager.storemanager.model.Customer customer, List<SaleDetail> details,
                            String paymentMethod, String observation) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("No hay usuario autenticado.");
        }
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un producto al carrito.");
        }
        Sale sale = new Sale();
        sale.setUser(user);
        sale.setCustomer(customer);
        sale.setDetails(details);
        sale.setPaymentMethod(paymentMethod);
        sale.setStatus("COMPLETADA");
        sale.setObservation(observation == null ? null : observation.trim());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (SaleDetail detail : details) {
            BigDecimal lineSubtotal = detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
            BigDecimal lineTax = lineSubtotal.multiply(AppConfig.TAX_RATE).setScale(2, RoundingMode.HALF_UP);
            detail.setUnitTax(detail.getUnitPrice().multiply(AppConfig.TAX_RATE).setScale(2, RoundingMode.HALF_UP));
            detail.setLineSubtotal(lineSubtotal.setScale(2, RoundingMode.HALF_UP));
            detail.setLineTotal(lineSubtotal.add(lineTax).setScale(2, RoundingMode.HALF_UP));
            subtotal = subtotal.add(detail.getLineSubtotal());
            tax = tax.add(lineTax);
        }
        sale.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        sale.setTax(tax.setScale(2, RoundingMode.HALF_UP));
        sale.setTotal(subtotal.add(tax).setScale(2, RoundingMode.HALF_UP));
        return sale;
    }

    public Long createSale(Sale sale) throws SQLException {
        return saleDao.createSale(sale);
    }

    public List<Sale> findTodaySales() throws SQLException {
        return saleDao.findSalesByDateRange(LocalDate.now(), LocalDate.now());
    }

    public List<Sale> findSalesByDateRange(LocalDate from, LocalDate to) throws SQLException {
        return saleDao.findSalesByDateRange(from, to);
    }
}
