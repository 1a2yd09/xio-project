package com.cat.utils;

import com.cat.entity.bean.WorkOrder;

/**
 * @author CAT
 */
public class OrderUtils {
    private OrderUtils() {
    }

    /**
     * 转换表示数目的字符串类型属性为整型类型
     *
     * @param property 表示数目的字符串类型的属性
     * @return 结果
     */
    public static int amountPropStrToInt(String property) {
        try {
            return Integer.parseInt(property);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 相加一个表示数目的字符串类型的属性和整型类型变量
     *
     * @param property 表示数目的字符串类型的属性
     * @param amount   整型变量
     * @return 结果
     */
    public static String addAmountPropWithInt(String property, int amount) {
        return String.valueOf(amountPropStrToInt(property) + amount);
    }

    public static WorkOrder getFakeOrder() {
        return new WorkOrder("0.00×0.00×0.00", "0", "无", -1, "0");
    }
}
