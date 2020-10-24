package com.cat;

import com.cat.entity.OperatingParameter;
import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BoardCategory;
import com.cat.entity.enums.BottomSortPattern;
import com.cat.service.InventoryService;
import com.cat.service.ParameterService;
import com.cat.service.WorkOrderService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderServiceTest {
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
    void testSomething() {
        assertEquals(996, workOrderService.getOrderCount());
    }

    @Test
    void testGetBottomOrder() {
        OperatingParameter op = parameterService.getOperatingParameter();
        LocalDate date = op.getWorkOrderDate();
        List<WorkOrder> orders = workOrderService.getBottomOrders(BottomSortPattern.SPEC.value, date);
        assertEquals(914, orders.size());
    }

    @Test
    void testGetNotBottomOrder() {
        OperatingParameter op = parameterService.getOperatingParameter();
        LocalDate date = op.getWorkOrderDate();
        List<WorkOrder> orders = workOrderService.getNotBottomOrders(date);
        assertEquals(82, orders.size());
        inventoryService.addNewInventory("4.00×245.00×3190.00", "热板", 7, BoardCategory.STOCK.value);
        inventoryService.addNewInventory("4.00×245.00×3150.00", "热板", 7, BoardCategory.STOCK.value);
        orders = workOrderService.getPreprocessNotBottomOrders(date);
        assertEquals(76, orders.size());
    }
}
