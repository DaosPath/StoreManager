package com.manager.storemanager.config;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public final class AppConfig {

    public static final String APPLICATION_NAME = "StoreManager";
    public static final String DATABASE_PROPERTIES = "/db.properties";
    public static final BigDecimal TAX_RATE = new BigDecimal("0.19");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private AppConfig() {
    }

    public static String buildInvoiceNumber(long sequence) {
        return "V-" + String.format("%08d", sequence);
    }
}
