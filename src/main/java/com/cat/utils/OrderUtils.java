package com.cat.utils;

import com.cat.entity.bean.WorkOrder;

/**
 * @author CAT
 */
public class OrderUtils {
    private OrderUtils() {
    }

    /**
     * 转换数量字符串为整型。
     *
     * @param property 数量字符串
     * @return 结果
     */
    public static int quantityPropStrToInt(String property) {
        try {
            return Integer.parseInt(property);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 相加一个表示数目的字符串类型的属性和整型类型变量。
     *
     * @param property 表示数目的字符串类型的属性
     * @param quantity 整型变量
     * @return 结果
     */
    public static String addQuantityPropWithInt(String property, int quantity) {
        return String.valueOf(quantityPropStrToInt(property) + quantity);
    }

    public static WorkOrder getFakeOrder() {
        return new WorkOrder("0.00×0.00×0.00", "0", "无", -1, "0");
    }
}
