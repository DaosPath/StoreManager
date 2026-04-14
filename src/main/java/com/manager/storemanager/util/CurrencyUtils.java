package com.manager.storemanager.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;

public final class CurrencyUtils {

    private static final Locale CURRENCY_LOCALE = new Locale("es", "PE");
    private static final Currency PEN = Currency.getInstance("PEN");
    private static final String CURRENCY_SYMBOL = "S/";

    private CurrencyUtils() {
    }

    public static String format(BigDecimal value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(CURRENCY_LOCALE);
        symbols.setCurrencySymbol(CURRENCY_SYMBOL);

        DecimalFormat format = new DecimalFormat("\u00A4 #,##0.00", symbols);
        format.setCurrency(PEN);
        return format.format(value == null ? BigDecimal.ZERO : value);
    }

    public static String symbol() {
        return CURRENCY_SYMBOL;
    }
}
