package com.cat;

import com.cat.pojo.WorkOrder;
import com.cat.pojo.CutBoard;
import com.cat.pojo.NormalBoard;
import com.cat.pojo.OperatingParameter;
import com.cat.enums.BoardCategory;
import com.cat.enums.OrderSortPattern;
import com.cat.enums.OrderState;
import com.cat.service.InventoryService;
import com.cat.service.OrderService;
import com.cat.service.ParameterService;
import com.cat.utils.OrderUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderTest extends BaseTest {
    @Autowired
    OrderService orderService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    InventoryService inventoryService;

    @Test
    void testUtils() {
        assertEquals(0, OrderUtil.quantityPropStrToInt("null"));
        assertEquals(0, OrderUtil.quantityPropStrToInt(null));
        assertEquals(3, OrderUtil.quantityPropStrToInt("3"));
        assertEquals("7", OrderUtil.addQuantityPropWithInt("3", 4));
    }

    @Test
    void testGetAllWidthBetterBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        LocalDate date = op.getOrderDate();
        List<WorkOrder> orders = orderService.getBottomOrders(OrderSortPattern.BY_SPEC.value, date);
        assertEquals(677, orders.size());
        for (WorkOrder order : orders) {
            CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial());
            NormalBoard board = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
            if (board.getWidth().compareTo(cutBoard.getWidth()) > 0) {
                System.out.println(order);
            }
        }
    }

    @Test
    void testGetAllWidthBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        LocalDate date = op.getOrderDate();
        List<WorkOrder> orders = orderService.getBottomOrders(OrderSortPattern.BY_SPEC.value, date);
        assertEquals(677, orders.size());
        for (WorkOrder order : orders) {
            CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial());
            NormalBoard board = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
            if (cutBoard.getWidth().compareTo(new BigDecimal(900)) < 0 || board.getLength().compareTo(new BigDecimal(900)) < 0) {
                System.out.println(order);
            }
        }
    }

    @Test
    void testGetAllWidthBetterNotBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        List<WorkOrder> orders = orderService.getNotBottomOrders(op.getOrderDate());
        assertEquals(82, orders.size());
        for (WorkOrder order : orders) {
            CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial());
            NormalBoard board = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
            if (board.getWidth().compareTo(cutBoard.getWidth()) > 0) {
                System.out.println(order);
            }
        }
    }

    @Test
    void testGetAllWidthNotBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        List<WorkOrder> orders = orderService.getNotBottomOrders(op.getOrderDate());
        assertEquals(82, orders.size());
        for (WorkOrder order : orders) {
            CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial());
            NormalBoard board = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
            if (board.getLength().compareTo(new BigDecimal(900)) < 0 || cutBoard.getWidth().compareTo(new BigDecimal(900)) < 0) {
                System.out.println(order);
            }
        }
    }

    @Test
    @Rollback
    @Transactional
    void testGetNotBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        LocalDate date = op.getOrderDate();
        // 获取未预处理的直梁工单，共82个
        List<WorkOrder> orders = orderService.getNotBottomOrders(date);
        assertEquals(82, orders.size());
        inventoryService.insertInventory("4.00×245.00×3190.00", "热板", 7, BoardCategory.STOCK.value);
        inventoryService.insertInventory("4.00×245.00×3150.00", "热板", 7, BoardCategory.STOCK.value);
        // 获取经过预处理的直梁工单，其中前8个工单有6个因为使用了已有的库存件作为成品，因此未完工的工单数量为76个
        orders = orderService.getPreprocessNotBottomOrders(date);
        assertEquals(76, orders.size());
        orders = orderService.getNotBottomOrders(date);
        orders.forEach(System.out::println);
    }

    @Test
    @Rollback
    @Transactional
    void testAddOrderCompletedQuantity() {
        WorkOrder order = orderService.getOrderById(3098562);
        orderService.addOrderCompletedQuantity(order, Integer.parseInt(order.getProductQuantity()));
        assertEquals(0, order.getIncompleteQuantity());
        assertEquals(OrderState.COMPLETED.value, order.getOperationState());
    }

    @Test
    void testGetOrder() {
        Assertions.assertEquals(82, this.orderService.getNotBottomOrders(LocalDate.of(2019, 11, 13)).size());
        Assertions.assertEquals(677, this.orderService.getBottomOrders(OrderSortPattern.BY_SPEC.value, LocalDate.of(2019, 11, 13)).size());
    }
}
