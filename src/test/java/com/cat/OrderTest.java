package com.cat;

import com.cat.entity.bean.WorkOrder;
import com.cat.entity.board.CutBoard;
import com.cat.entity.board.NormalBoard;
import com.cat.entity.param.OperatingParameter;
import com.cat.enums.BoardCategory;
import com.cat.enums.OrderSortPattern;
import com.cat.enums.OrderState;
import com.cat.service.InventoryService;
import com.cat.service.OrderService;
import com.cat.service.ParameterService;
import com.cat.utils.OrderUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

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
        assertEquals(0, OrderUtils.amountPropStrToInt("null"));
        assertEquals(0, OrderUtils.amountPropStrToInt(null));
        assertEquals(3, OrderUtils.amountPropStrToInt("3"));
        assertEquals("7", OrderUtils.addAmountPropWithInt("3", 4));
    }

    @Test
    void testGetAllWidthBetterBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        LocalDate date = op.getWorkOrderDate();
        List<WorkOrder> orders = orderService.getBottomOrders(OrderSortPattern.BY_SPEC.value, date);
        assertEquals(914, orders.size());
        for (WorkOrder order : orders) {
            CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial());
            NormalBoard board = new NormalBoard(order.getSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
            if (board.getWidth().compareTo(cutBoard.getWidth()) > 0) {
                System.out.println(order);
            }
        }
    }

    @Test
    void testGetAllWidthBetterNotBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        List<WorkOrder> orders = orderService.getNotBottomOrders(op.getWorkOrderDate());
        assertEquals(82, orders.size());
        for (WorkOrder order : orders) {
            CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial());
            NormalBoard board = new NormalBoard(order.getSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
            if (board.getWidth().compareTo(cutBoard.getWidth()) > 0) {
                System.out.println(order);
            }
        }
    }

    @Test
    @Rollback
    @Transactional
    void testGetNotBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        LocalDate date = op.getWorkOrderDate();
        // 获取未预处理的直梁工单，共82个
        List<WorkOrder> orders = orderService.getNotBottomOrders(date);
        assertEquals(82, orders.size());
        inventoryService.insertInventory("4.00×245.00×3190.00", "热板", 7, BoardCategory.STOCK.value);
        inventoryService.insertInventory("4.00×245.00×3150.00", "热板", 7, BoardCategory.STOCK.value);
        // 获取经过预处理的直梁工单，其中前8个工单有6个因为使用了已有的库存件作为成品，因此未完工的工单数量为76个
        orders = orderService.getPreprocessNotBottomOrders(date);
        assertEquals(76, orders.size());
    }

    @Test
    @Rollback
    @Transactional
    void testAddOrderCompletedAmount() {
        WorkOrder order = orderService.getOrderById(3098562);
        orderService.addOrderCompletedAmount(order, Integer.parseInt(order.getAmount()));
        assertEquals(0, order.getUnfinishedAmount());
        assertEquals(OrderState.COMPLETED.value, order.getOperationState());
    }
}
