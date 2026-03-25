package com.manager.storemanager.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailySalesTotal {

    private LocalDate date;
    private BigDecimal total;

    public DailySalesTotal(LocalDate date, BigDecimal total) {
        this.date = date;
        this.total = total;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
