package com.cat;

import com.cat.pojo.WorkOrder;
import com.cat.service.ActionService;
import com.cat.service.MainService;
import com.cat.service.OrderService;
import com.cat.service.ParameterService;
import com.cat.service.impl.BottomModuleServiceImpl;
import com.cat.utils.SignalUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BottomProcessTest extends BaseTest {
    @Autowired
    MainService mainService;
    @Autowired
    OrderService orderService;
    @Autowired
    ActionService actionService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    BottomModuleServiceImpl bottomModuleServiceImpl;

    @Test
    void test1() {
        WorkOrder order = orderService.getOrderById(3099510);
        bottomModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order);
        actionService.getAllMachineActions().forEach(System.out::println);
        assertEquals(10, actionService.getAllMachineActions().size());
    }

    @Test
    void test2() {
        WorkOrder order = orderService.getOrderById(3099510);
        order.setProductSpecification("2.5×121×2504");
        bottomModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order);
        actionService.getAllMachineActions().forEach(System.out::println);
        assertEquals(7, actionService.getAllMachineActions().size());
    }

    @Test
    void test3() {
        WorkOrder order = orderService.getOrderById(3099510);
        order.setProductQuantity("8");
        order.setCuttingSize("2.5×1000×2504");
        bottomModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order);
        actionService.getAllMachineActions().forEach(System.out::println);
        assertEquals(9, actionService.getAllMachineActions().size());
    }

    @Test
    void test4() {
        WorkOrder order = orderService.getOrderById(3099510);
        order.setProductQuantity("8");
        order.setProductSpecification("2.5×240×2185");
        bottomModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order);
        actionService.getAllMachineActions().forEach(System.out::println);
        assertEquals(7, actionService.getAllMachineActions().size());
    }

    @Test
    void test5() {
        WorkOrder order = orderService.getOrderById(3099510);
        order.setProductQuantity("15");
        order.setProductSpecification("2.5×100×2185");
        bottomModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order);
        actionService.getAllMachineActions().forEach(System.out::println);
        assertEquals(13, actionService.getAllMachineActions().size());
    }
}
