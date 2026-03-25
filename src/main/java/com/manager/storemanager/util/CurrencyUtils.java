package com.manager.storemanager.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyUtils {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private CurrencyUtils() {
    }

    public static String format(BigDecimal value) {
        return CURRENCY_FORMAT.format(value == null ? BigDecimal.ZERO : value);
    }
}
