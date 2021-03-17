package com.cat.utils;

import java.math.BigDecimal;

/**
 * @author CAT
 */
public class DecimalUtil {
    private DecimalUtil() {

    }

    public static int cmp(BigDecimal v1, BigDecimal v2) {
        return v1.compareTo(v2);
    }

    public static BigDecimal sub(BigDecimal v1, BigDecimal v2) {
        return v1.subtract(v2);
    }

    public static BigDecimal mul(BigDecimal v1, Integer v2) {
        return v1.multiply(new BigDecimal(v2));
    }

    public static int div(BigDecimal v1, BigDecimal v2) {
        return v1.divideToIntegralValue(v2).intValue();
    }

    public static BigDecimal max(BigDecimal v1, BigDecimal v2) {
        return v1.compareTo(v2) >= 0 ? v1 : v2;
    }
}
