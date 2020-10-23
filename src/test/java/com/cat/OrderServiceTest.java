package com.cat;

import com.cat.entity.Board;
import com.cat.entity.OperatingParameter;
import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BoardCategory;
import com.cat.service.InventoryService;
import com.cat.service.ParameterService;
import com.cat.service.WorkOrderService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderServiceTest {
    final Logger logger = LoggerFactory.getLogger(getClass());

    static ApplicationContext context;
    static WorkOrderService workOrderService;
    static ParameterService parameterService;
    static InventoryService inventoryService;

    @BeforeAll
    public static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        workOrderService = context.getBean(WorkOrderService.class);
        parameterService = context.getBean(ParameterService.class);
        inventoryService = context.getBean(InventoryService.class);
    }

    @Test
    void testCopyOrder() {
        workOrderService.truncateOrderTable();
        assertEquals(0, workOrderService.getOrderCount());
        LocalDate date = parameterService.getLatestOperatingParameter().getWorkOrderDate();
        workOrderService.copyRemoteOrderToLocal(date);
        assertEquals(996, workOrderService.getOrderCount());
    }

    @Test
    void testGetBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        String sortPattern = op.getBottomOrderSort();
        logger.info("sortPattern: {}", sortPattern);
        LocalDate date = op.getWorkOrderDate();
        logger.info("date: {}", date);
        List<WorkOrder> orders = workOrderService.getBottomOrders(sortPattern, date);
        assertEquals(914, orders.size());
        orders.forEach(System.out::println);
    }

    @Test
    void testGetNotBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        LocalDate date = op.getWorkOrderDate();
        List<WorkOrder> orders = workOrderService.getNotBottomOrders(date);
        assertEquals(82, orders.size());
        orders.forEach(System.out::println);
    }

    /**
     * 测试预处理直梁工单，逻辑就是如果存货中有和成品规格、材质相同的库存件，就可以作为该直梁工单的成品。
     */
    @Test
    void testPreprocessNotBottomOrder() {
        // 成品规格:4.0×245×3190，需求数:2个，已完成数目:0个
        WorkOrder order = workOrderService.getOrderById(3098562);
        Board stock = new Board(order.getSpecification(), order.getMaterial(), BoardCategory.STOCK);
        inventoryService.addInventory(stock, 9);
        LocalDate date = parameterService.getLatestOperatingParameter().getWorkOrderDate();
        List<WorkOrder> orders = workOrderService.getPreprocessNotBottomOrder(date);
    }
}
