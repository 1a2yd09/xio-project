package com.cat.utils;

import com.cat.enums.OrderSortPattern;
import com.cat.pojo.WorkOrder;

import java.util.Comparator;
import java.util.List;

/**
 * @author CAT
 */
public class OrderUtil {
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

    public static WorkOrder getFakeOrder() {
        return new WorkOrder("0.00×0.00×0.00", "0", "无", -1, "0");
    }

    public static void sortOrderList(List<WorkOrder> orders, OrderSortPattern sortPattern) {
        switch (sortPattern) {
            case SEQ:
                orders.sort(OrderUtil::compareOrderSeq);
                break;
            case SPEC:
                orders.sort(OrderUtil::compareOrderSpec);
                break;
            case PCH_SEQ:
                orders.sort((o1, o2) -> compareOrderBatchNum(o1, o2) == 0 ? compareOrderSeq(o1, o2) : compareOrderBatchNum(o1, o2));
                break;
            case PCH_SPEC:
                orders.sort((o1, o2) -> compareOrderBatchNum(o1, o2) == 0 ? compareOrderSpec(o1, o2) : compareOrderBatchNum(o1, o2));
                break;
            default:
                orders.sort(Comparator.comparingInt(WorkOrder::getId));
                break;
        }
    }

    private static int compareOrderSeq(WorkOrder o1, WorkOrder o2) {
        Integer sn1 = Integer.parseInt(o1.getSequenceNumber());
        Integer sn2 = Integer.parseInt(o2.getSequenceNumber());
        return sn1.equals(sn2) ? o1.getId() - o2.getId() : sn1.compareTo(sn2);
    }

    private static int compareOrderSpec(WorkOrder o1, WorkOrder o2) {
        int retVal = BoardUtil.compareTwoSpecStr(o1.getProductSpecification(), o2.getProductSpecification());
        return retVal != 0 ? -retVal : o1.getId() - o2.getId();
    }

    private static int compareOrderBatchNum(WorkOrder o1, WorkOrder o2) {
        Integer bn1 = Integer.parseInt(o1.getBatchNumber());
        Integer bn2 = Integer.parseInt(o2.getBatchNumber());
        return bn1.equals(bn2) ? 0 : bn1 - bn2;
    }
}
