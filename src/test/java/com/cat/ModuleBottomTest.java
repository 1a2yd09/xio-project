package com.cat;

import com.cat.enums.OrderModule;
import com.cat.enums.OrderSortPattern;
import com.cat.pojo.CuttingSignal;
import com.cat.pojo.OperatingParameter;
import com.cat.pojo.WorkOrder;
import com.cat.service.ActionService;
import com.cat.service.MainService;
import com.cat.service.OrderService;
import com.cat.service.ParameterService;
import com.cat.service.impl.BottomModuleServiceImpl;
import com.cat.utils.SignalUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleBottomTest extends BaseTest {
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

    /**
     * 成品宽度大于深度且总宽大于宽度。
     */
    @Test
    void test1() {
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "10", LocalDateTime.now(), 20000001, "2021050101", "1", "4×1500×3600", "轿底吊顶工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000001);
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.BOTTOM_PLATFORM.getName()));
        bottomModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order);
        actionService.getAllMachineActions().forEach(System.out::println);
        // 长度修边(3600->3190)-旋转-进刀废料(1500-245*6=30)-进刀5个成品(245)-送成品:
        assertEquals(9, actionService.getAllMachineActions().size());
    }

    /**
     * 成品宽度大于深度且总宽大于宽度，有半成品。
     */
    @Test
    void test2() {
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "3", LocalDateTime.now(), 20000002, "2021050101", "1", "4×1500×3600", "轿底吊顶工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000002);
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.BOTTOM_PLATFORM.getName()));
        bottomModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order);
        actionService.getAllMachineActions().forEach(System.out::println);
        // 旋转-进刀废料(1500-245*3-192*3=189)-进刀3个半成品(192)-进刀2个成品(245)-送成品:
        assertEquals(11, actionService.getAllMachineActions().size());
    }

    /**
     * 成品宽度大于深度且总宽大于宽度，修边值为100影响到成品数量。
     */
    @Test
    void test3() {
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "10", LocalDateTime.now(), 20000003, "2021050101", "1", "4×1500×3600", "轿底吊顶工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000003);
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.BOTTOM_PLATFORM.getName()));
        CuttingSignal cuttingSignal = SignalUtil.getDefaultCuttingSignal(order);
        cuttingSignal.setLongEdgeTrim(new BigDecimal("100"));
        bottomModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), cuttingSignal, order);
        actionService.getAllMachineActions().forEach(System.out::println);
        // 1500最多裁剪6个245成品，考虑有一条较长边未修边，如果修边值在100，则实际可用宽度只有1400，只能裁剪5个成品，且无法裁剪半成品，
        // 长度修边(3600->3190)-旋转-进刀废料(1500-245*5=275)-进刀4个成品(245)-送成品:
        assertEquals(8, actionService.getAllMachineActions().size());
    }

    /**
     * 成品宽度小于深度且总宽小于宽度，需预留修边宽度。
     */
    @Test
    void test4() {
        orderService.insertOrder(new WorkOrder("未开工", "4×135×3190", "热板", "3", LocalDateTime.now(), 20000004, "2021050101", "1", "4×1500×3600", "轿底吊顶工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000004);
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.BOTTOM_PLATFORM.getName()));
        bottomModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order);
        actionService.getAllMachineActions().forEach(System.out::println);
        // 旋转-进刀废料(1500-1490)-进刀4个半成品(192)-旋转-长度修边-旋转-进刀3个成品-送余料板:
        assertEquals(13, actionService.getAllMachineActions().size());
    }

    /**
     * 成品宽度大于深度且总宽小于宽度，需预留修边宽度。
     */
    @Test
    void test5() {
        orderService.insertOrder(new WorkOrder("未开工", "4×250×3190", "热板", "1", LocalDateTime.now(), 20000005, "2021050101", "1", "4×1500×3600", "轿底吊顶工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000005);
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.BOTTOM_PLATFORM.getName()));
        bottomModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order);
        actionService.getAllMachineActions().forEach(System.out::println);
        // 旋转-进刀废料(1500-1490)-进刀5个半成品(192)-旋转-长度修边-旋转-进刀1个成品-送余料板:
        assertEquals(12, actionService.getAllMachineActions().size());
    }

    /**
     * 成品宽度小于深度且总宽大于宽度。
     */
    @Test
    void test6() {
        orderService.insertOrder(new WorkOrder("未开工", "4×135×3190", "热板", "4", LocalDateTime.now(), 20000006, "2021050101", "1", "4×1500×3600", "轿底吊顶工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000006);
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.BOTTOM_PLATFORM.getName()));
        bottomModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order);
        actionService.getAllMachineActions().forEach(System.out::println);
        // 旋转-进刀废料(1500-1490)-进刀3个半成品(192)-旋转-长度修边-旋转-进刀4个成品-送余料板:
        assertEquals(13, actionService.getAllMachineActions().size());
    }
}
