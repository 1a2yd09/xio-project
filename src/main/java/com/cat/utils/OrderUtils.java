package com.cat.utils;

import com.cat.entity.bean.WorkOrder;

public class OrderUtils {
    private OrderUtils() {
    }

    public static int amountPropStrToInt(String property) {
        return property == null ? 0 : Integer.parseInt(property);
    }

    public static String addAmountPropWithInt(String property, int amount) {
        return String.valueOf(amountPropStrToInt(property) + amount);
    }

    public static WorkOrder getFakeOrder() {
        return new WorkOrder("0.00×0.00×0.00", "0", "无", -1, "0");
    }
}
