package com.cat.util;

public class OrderUtil {
    private OrderUtil() {
    }

    public static int amountPropertyStrToInt(String property) {
        return property == null ? 0 : Integer.parseInt(property);
    }
}
