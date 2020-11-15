package com.cat;

import com.cat.entity.CutBoard;
import com.cat.entity.NormalBoard;
import com.cat.entity.OperatingParameter;
import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BoardCategory;
import com.cat.entity.enums.OrderSortPattern;
import com.cat.service.InventoryService;
import com.cat.service.ParameterService;
import com.cat.service.OrderService;
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
    void testSomething() {
        assertEquals(996, orderService.getOrderCount());
    }

    @Test
    void testGetBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        LocalDate date = op.getWorkOrderDate();
        List<WorkOrder> orders = orderService.getBottomOrders(OrderSortPattern.BY_SPEC.value, date);
        assertEquals(914, orders.size());
        for (WorkOrder order : orders) {
            CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial());
            NormalBoard board = new NormalBoard(order.getSpecStr(), order.getMaterial(), BoardCategory.PRODUCT);
            if (board.getWidth().compareTo(cutBoard.getWidth()) == 0) {
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
            NormalBoard board = new NormalBoard(order.getSpecStr(), order.getMaterial(), BoardCategory.PRODUCT);
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
        List<WorkOrder> orders = orderService.getNotBottomOrders(date);
        // 获取未预处理的直梁工单，共82个:
        assertEquals(82, orders.size());
        inventoryService.addNewInventory("4.00×245.00×3190.00", "热板", 7, BoardCategory.STOCK.value);
        inventoryService.addNewInventory("4.00×245.00×3150.00", "热板", 7, BoardCategory.STOCK.value);
        orders = orderService.getPreprocessNotBottomOrders(date);
        // 获取预处理的直梁工单，其中有6个工单因为使用了已有的库存件作为成品，因此工单数量变为76个:
        assertEquals(76, orders.size());
    }
}
