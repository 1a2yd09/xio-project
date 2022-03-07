package com.cat.utils;

import com.cat.enums.BoardCategory;
import com.cat.pojo.NormalBoard;
import com.cat.pojo.WorkOrder;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author CAT
 */
public class OrderUtil {
    private static final WorkOrder FAKE_STRAIGHT_ORDER = new WorkOrder("999×999×999", "无", "0", -1);

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

    public static List<WorkOrder> filterOrderList(List<WorkOrder> orderList) {
        WorkOrder firstOrder = orderList.get(0);
        BigDecimal firstHeight = BoardUtil.specStrToDecList(firstOrder.getProductSpecification()).get(0);
        return orderList.stream()
                .filter(order -> order.getBatchNumber().equals(firstOrder.getBatchNumber()))
                .filter(order -> order.getMaterial().equals(firstOrder.getMaterial()))
                .filter(order -> BoardUtil.specStrToDecList(order.getProductSpecification()).get(0).compareTo(firstHeight) == 0)
                .filter(order -> BoardUtil.compareTwoSpecStr(order.getCuttingSize(), firstOrder.getCuttingSize()) == 0)
                .collect(Collectors.toList());
    }

    public static List<WorkOrder> filterOrderList(Integer orderId, List<WorkOrder> orderList) {
        Iterator<WorkOrder> iterator = orderList.iterator();
        // 遍历工单，对比下料信号中的工单ID，排除ID不一致的工单，直到遇到ID一致的工单:
        while (iterator.hasNext()) {
            if (!iterator.next().getId().equals(orderId)) {
                iterator.remove();
            } else {
                break;
            }
        }

        WorkOrder firstOrder = orderList.get(0);
        BigDecimal firstHeight = BoardUtil.specStrToDecList(firstOrder.getProductSpecification()).get(0);
        return orderList.stream()
                .filter(order -> order.getBatchNumber().equals(firstOrder.getBatchNumber()))
                .filter(order -> order.getMaterial().equals(firstOrder.getMaterial()))
                .filter(order -> BoardUtil.specStrToDecList(order.getProductSpecification()).get(0).compareTo(firstHeight) == 0)
                .filter(order -> BoardUtil.compareTwoSpecStr(order.getCuttingSize(), firstOrder.getCuttingSize()) == 0)
                .collect(Collectors.toList());
    }

    public static Map<Integer, Integer> calOrderProduct(List<NormalBoard> boardList) {
        Map<Integer, Integer> countMap = new HashMap<>();
        for (NormalBoard normalBoard : boardList) {
            if (normalBoard.getCategory() == BoardCategory.PRODUCT) {
                countMap.put(normalBoard.getOrderId(), countMap.getOrDefault(normalBoard.getOrderId(), 0) + normalBoard.getCutTimes());
            }
        }
        return countMap;
    }

    public static Integer getNextOrderId(Map<Integer, Integer> countMap, List<WorkOrder> orderList) {
        for (WorkOrder order : orderList) {
            if (countMap.containsKey(order.getId())) {
                if (order.getIncompleteQuantity() > countMap.get(order.getId())) {
                    return order.getId();
                }
            } else {
                return order.getId();
            }
        }
        return null;
    }
}
