package com.cat.utils;

import com.cat.pojo.WorkOrder;
import org.springframework.util.StringUtils;

/**
 * @author CAT
 */
public class OrderUtil {
    private static final WorkOrder FAKE_STRAIGHT_ORDER = new WorkOrder("0.00×0.00×0.00", "0", "无", -1, "0");

    private OrderUtil() {
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

    /**
     * 获取一个“不存在”的工单对象。
     *
     * @return 工单对象
     */
    public static WorkOrder getFakeOrder() {
        return FAKE_STRAIGHT_ORDER;
    }

    /**
     * 验证工单对象的成品尺寸、材质、原料尺寸是否为空或无内容。
     *
     * @param order 工单对象
     * @return true 表示工单对象上述三个属性都包含内容，否则至少一个属性不包含内容
     */
    public static boolean validateOrder(WorkOrder order) {
        return validateStr(order.getProductSpecification()) && validateStr(order.getMaterial()) && validateStr(order.getCuttingSize());
    }

    /**
     * 验证字符串是否包含内容
     *
     * @param s 字符串对象
     * @return true 表示该字符串包含内容，否则不包含内容
     */
    private static boolean validateStr(String s) {
        return StringUtils.hasText(s);
    }
}
