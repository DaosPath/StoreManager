package com.manager.storemanager.model;

import java.math.BigDecimal;

public class DashboardSummary {

    private int productCount;
    private int customerCount;
    private int supplierCount;
    private int lowStockCount;
    private BigDecimal salesToday = BigDecimal.ZERO;

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public int getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(int customerCount) {
        this.customerCount = customerCount;
    }

    public int getSupplierCount() {
        return supplierCount;
    }

    public void setSupplierCount(int supplierCount) {
        this.supplierCount = supplierCount;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public BigDecimal getSalesToday() {
        return salesToday;
    }

    public void setSalesToday(BigDecimal salesToday) {
        this.salesToday = salesToday;
    }

    public BigDecimal getTodaySalesTotal() {
        return salesToday;
    }

    public void setTodaySalesTotal(BigDecimal todaySalesTotal) {
        this.salesToday = todaySalesTotal;
    }
}
