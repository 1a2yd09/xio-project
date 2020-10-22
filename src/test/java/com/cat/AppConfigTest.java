package com.cat;

import com.cat.entity.Board;
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

class AppConfigTest {
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
    void testSomething() {

    }

    @Test
    void testGetAllWidthBetterBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        List<WorkOrder> orders = workOrderService.getBottomOrders(op.getWorkOrderDate());
        assertEquals(914, orders.size());
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
