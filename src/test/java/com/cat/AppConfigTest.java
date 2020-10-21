package com.cat;

import com.cat.entity.Board;
import com.cat.entity.CutBoard;
import com.cat.entity.OperatingParameter;
import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BoardCategory;
import com.cat.service.ParameterService;
import com.cat.service.WorkOrderService;
import com.cat.util.BoardUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppConfigTest {
    final Logger logger = LoggerFactory.getLogger(getClass());

    static ApplicationContext context;
    static WorkOrderService workOrderService;
    static ParameterService parameterService;

    @BeforeAll
    public static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        workOrderService = context.getBean(WorkOrderService.class);
        parameterService = context.getBean(ParameterService.class);
    }

    @Test
    public void testSomething() {
        WorkOrder order = workOrderService.getWorkOrderById(3099510);
        System.out.println(order);
        workOrderService.updateOrderCompletedAmount(order);
        order = workOrderService.getWorkOrderById(3099510);
        System.out.println(order);
    }

    @Test
    public void testGetAllWidthBetterBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        List<WorkOrder> orders = workOrderService.getBottomOrders(op.getWorkOrderDate());
        assertEquals(orders.size(), 914);
        for (WorkOrder order : orders) {
            Board board = new Board(order.getSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
            if (board.getWidth().compareTo(board.getLength()) >= 0) {
                System.out.println(board);
                BoardUtil.standardizingBoard(board);
                System.out.println(board);
            }
        }
    }

    @Test
    public void testCompareBoard() {
        int orderId = 3105787;
        WorkOrder order = workOrderService.getWorkOrderById(orderId);
        logger.info("order: {}", order);
        CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial(), BoardCategory.CUTTING);
        Board productBoard = new Board(order.getSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
        assertEquals(cutBoard.compareTo(productBoard), 1);
        assertEquals(productBoard.compareTo(cutBoard), -1);
        productBoard.setMaterial("热板");
        assertEquals(cutBoard.compareTo(productBoard), -1);
    }
}
