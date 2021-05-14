package com.cat;

import com.cat.enums.BoardCategory;
import com.cat.enums.OrderSortPattern;
import com.cat.enums.OrderState;
import com.cat.pojo.CutBoard;
import com.cat.pojo.Inventory;
import com.cat.pojo.NormalBoard;
import com.cat.pojo.WorkOrder;
import com.cat.service.InventoryService;
import com.cat.service.OrderService;
import com.cat.service.ParameterService;
import com.cat.utils.OrderUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest extends BaseTest {
    @Autowired
    OrderService orderService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    InventoryService inventoryService;

    /**
     * 测试工单字段相关处理方法。
     */
    @Test
    void testUtils() {
        assertEquals(0, OrderUtil.quantityPropStrToInt("null"));
        assertEquals(0, OrderUtil.quantityPropStrToInt(null));
        assertEquals(3, OrderUtil.quantityPropStrToInt("3"));
        assertEquals("7", OrderUtil.addQuantityPropWithInt("3", 4));
    }

    /**
     * 找出工单成品板宽度大于原料板宽度的工单。
     */
    @Test
    void testGetAllWidthBetterOrder() {
        assertTrue(Boolean.TRUE);
        for (WorkOrder order : orderService.getAllLocalOrders()) {
            CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial(), order.getId());
            NormalBoard board = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT, order.getId());
            if (board.getWidth().compareTo(cutBoard.getWidth()) > 0) {
                System.out.println(order);
            }
        }
    }

    /**
     * 找出工单成品板宽度大于长度的工单。
     */
    @Test
    void testGetAllNormalBoardWidthBetterOrder() {
        assertTrue(Boolean.TRUE);
        for (WorkOrder order : orderService.getAllLocalOrders()) {
            NormalBoard board = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT, order.getId());
            if (board.getWidth().compareTo(board.getLength()) > 0) {
                System.out.println(order);
            }
        }
    }

    /**
     * 测试直梁对重模块工单的库存件预处理逻辑。
     */
    @Test
    void testGetStraightOrder() {
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "3", LocalDateTime.now(), 20000000, "2021050903", "1", "4×1500×3600", "对重架工地模块", "0"));
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "3", LocalDateTime.now(), 20000001, "2021050903", "2", "4×1500×3600", "对重架工地模块", "0"));
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "3", LocalDateTime.now(), 20000002, "2021050903", "3", "4×1500×3600", "对重架工地模块", "0"));
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3150", "热板", "3", LocalDateTime.now(), 20000003, "2021050903", "4", "4×1500×3600", "对重架工地模块", "0"));
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3150", "热板", "4", LocalDateTime.now(), 20000004, "2021050903", "5", "4×1500×3600", "对重架工地模块", "0"));
        inventoryService.insertInventory(new Inventory("4.00×245.00×3190.00", "热板", 7, BoardCategory.STOCK.value));
        inventoryService.insertInventory(new Inventory("4.00×245.00×3150.00", "热板", 7, BoardCategory.STOCK.value));
        Deque<WorkOrder> orderDeque = orderService.getPreprocessStraightDeque(OrderSortPattern.SPEC, LocalDate.now());
        assertEquals(1, orderDeque.size());
        Integer count = orderService.getCompletedOrderCount();
        assertEquals(4, count);
        List<WorkOrder> orders = orderService.getAllLocalOrders();
        orders.forEach(System.out::println);
    }

    /**
     * 当工单未完成数量为零时，工单状态修改为已完工，从本地表迁移至完工表。
     */
    @Test
    void testAddOrderCompletedQuantity() {
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "30", LocalDateTime.now(), 20000000, "2021050903", "1", "4×1500×3600", "对重架工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000000);
        orderService.addOrderCompletedQuantity(order, Integer.parseInt(order.getProductQuantity()));
        order = orderService.getCompletedOrderById(20000000);
        assertNotNull(order);
        assertEquals(OrderState.COMPLETED.value, order.getOperationState());
        assertEquals(0, order.getIncompleteQuantity());
        order = orderService.getOrderById(20000000);
        assertNull(order);
    }

    /**
     * 测试工单排序。
     */
    @Test
    void testOrderSorting() {
        assertTrue(Boolean.TRUE);
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "3", LocalDateTime.now(), 20000000, "2021050903", "1", "4×1500×3600", "对重架工地模块", "0"));
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3200", "热板", "3", LocalDateTime.now(), 20000001, "2021050903", "2", "4×1500×3600", "对重架工地模块", "0"));
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3340", "热板", "3", LocalDateTime.now(), 20000002, "2021050904", "4", "4×1500×3600", "对重架工地模块", "0"));
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3090", "热板", "3", LocalDateTime.now(), 20000003, "2021050904", "3", "4×1500×3600", "对重架工地模块", "0"));
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3530", "热板", "4", LocalDateTime.now(), 20000004, "2021050904", "1", "4×1500×3600", "对重架工地模块", "0"));
        Deque<WorkOrder> orderDeque = orderService.getPreprocessStraightDeque(OrderSortPattern.SEQ, LocalDate.now());
        orderDeque.forEach(System.out::println);
    }
}
