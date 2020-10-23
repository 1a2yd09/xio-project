package com.cat.util;

public class OrderUtil {
    private OrderUtil() {
    }

    public static int amountPropStrToInt(String property) {
        return property == null ? 0 : Integer.parseInt(property);
    }

    public static String addAmountPropWithInt(String property, int amount) {
        return String.valueOf(amountPropStrToInt(property) + amount);
    }
}
