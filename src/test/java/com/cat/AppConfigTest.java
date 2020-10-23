package com.cat;

import com.cat.entity.Board;
import com.cat.entity.CutBoard;
import com.cat.entity.OperatingParameter;
import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BoardCategory;
import com.cat.entity.enums.BottomSortPattern;
import com.cat.service.MachineActionService;
import com.cat.service.MainService;
import com.cat.service.ParameterService;
import com.cat.service.WorkOrderService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppConfigTest {
    final Logger logger = LoggerFactory.getLogger(getClass());

    static ApplicationContext context;
    static WorkOrderService workOrderService;
    static ParameterService parameterService;
    static MainService mainService;
    static MachineActionService actionService;

    @BeforeAll
    public static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        workOrderService = context.getBean(WorkOrderService.class);
        parameterService = context.getBean(ParameterService.class);
        mainService = context.getBean(MainService.class);
        actionService = context.getBean(MachineActionService.class);
    }

    @Test
    void testSomething() throws InterruptedException {
        mainService.startService();
        assertEquals(10898, actionService.getDoneActionCount());
    }

    @Test
    void testGetAllWidthBetterBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        List<WorkOrder> orders = workOrderService.getBottomOrders(BottomSortPattern.SEQ.value, op.getWorkOrderDate());
        assertEquals(914, orders.size());
        for (WorkOrder order : orders) {
            CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial(), BoardCategory.CUTTING);
            Board board = new Board(order.getSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
            if (board.getWidth().compareTo(cutBoard.getWidth()) == 0) {
                System.out.println(order);
            }
        }
    }

    @Test
    void testGetAllWidthBetterNotBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        List<WorkOrder> orders = workOrderService.getNotBottomOrders(op.getWorkOrderDate());
        assertEquals(82, orders.size());
        for (WorkOrder order : orders) {
            CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial(), BoardCategory.CUTTING);
            Board board = new Board(order.getSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
            if (board.getWidth().compareTo(cutBoard.getWidth()) > 0) {
                System.out.println(order);
            }
        }
    }

    @Test
    void testCompareBoard() {
        Board b1 = new Board("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        Board b2 = new Board("2.5×121×2185", "冷板", BoardCategory.PRODUCT);
        assertEquals(-1, b1.compareTo(b2));

        b1 = new Board("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        b2 = new Board("2×121×2185", "热板", BoardCategory.PRODUCT);
        assertEquals(-1, b1.compareTo(b2));

        b1 = new Board("2.5×120×2180", "热板", BoardCategory.PRODUCT);
        b2 = new Board("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        assertEquals(-1, b1.compareTo(b2));

        b1 = new Board("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        b2 = new Board("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        assertEquals(0, b1.compareTo(b2));

        b1 = new Board("2.5×122×2186", "热板", BoardCategory.PRODUCT);
        b2 = new Board("2.5×121×2185", "热板", BoardCategory.PRODUCT);
        assertEquals(1, b1.compareTo(b2));
    }
}
