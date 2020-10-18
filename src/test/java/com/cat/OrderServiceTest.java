package com.cat;

import com.cat.entity.OperatingParameter;
import com.cat.entity.WorkOrder;
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

public class OrderServiceTest {
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
    public void testGetBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        String sortPattern = op.getBottomOrderSort();
        logger.info("sortPattern: {}", sortPattern);
        LocalDate date = op.getWorkOrderDate();
        logger.info("date: {}", date);
        List<WorkOrder> orders = workOrderService.getBottomOrders(sortPattern, date);
        assertEquals(orders.size(), 914);
        orders.forEach(System.out::println);
    }

    @Test
    public void testCompletedAmount() {
        workOrderService.addOrderCompletedAmount(3101334, 1);
        WorkOrder order = workOrderService.getWorkOrderById(3101334);
        assertEquals(order.getUnfinishedAmount(), 0);
    }
}
