package com.cat.util;

import com.cat.entity.WorkOrder;

public class OrderUtil {
    private OrderUtil() {
    }

    public static int amountPropStrToInt(String property) {
        return property == null ? 0 : Integer.parseInt(property);
    }

    public static String addAmountPropWithInt(String property, int amount) {
        return String.valueOf(amountPropStrToInt(property) + amount);
    }

    public static WorkOrder getFakeOrder() {
        WorkOrder order = new WorkOrder();
        order.setSpecification("0.00×0.00×0.00");
        order.setMaterial("无");
        order.setAmount("0");
        order.setCompletedAmount("0");
        order.setId(-1);
        return order;
    }
}
