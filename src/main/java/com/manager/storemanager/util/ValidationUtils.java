package com.manager.storemanager.util;

import java.math.BigDecimal;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + fieldName + " es obligatorio.");
        }
    }

    public static void requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El campo " + fieldName + " debe ser mayor a cero.");
        }
    }

    public static void requireNonNegative(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El campo " + fieldName + " no puede ser negativo.");
        }
    }

    public static void requireNonNegativeInt(Integer value, String fieldName) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("El campo " + fieldName + " no puede ser negativo.");
        }
    }
}
