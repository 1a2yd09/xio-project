package com.cat.utils;

import java.math.BigDecimal;

/**
 * @author CAT
 */
public class Arith {
    private Arith() {

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
}
