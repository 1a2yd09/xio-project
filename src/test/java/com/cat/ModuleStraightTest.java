package com.cat;

import com.cat.enums.BoardCategory;
import com.cat.enums.OrderModule;
import com.cat.enums.OrderSortPattern;
import com.cat.pojo.NormalBoard;
import com.cat.pojo.OperatingParameter;
import com.cat.pojo.WorkOrder;
import com.cat.service.*;
import com.cat.service.impl.StraightModuleServiceImpl;
import com.cat.utils.OrderUtil;
import com.cat.utils.SignalUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleStraightTest extends BaseTest {
    @Autowired
    MainService mainService;
    @Autowired
    OrderService orderService;
    @Autowired
    ActionService actionService;
    @Autowired
    StockSpecService stockSpecService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    StraightModuleServiceImpl straightModuleServiceImpl;

    /**
     * 不是最后一次，宽度大于深度且总宽大于宽度。
     */
    @Test
    void test1() {
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "10", LocalDateTime.now(), 20000001, "2021050101", "1", "4×1500×3600", "对重架工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000001);
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.STRAIGHT_WEIGHT.getName()));
        straightModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order, OrderUtil.getFakeOrder());
        actionService.getAllMachineActions().forEach(System.out::println);
        // 长度修边-旋转-进刀废料-进刀5个成品-送成品:
        assertEquals(9, actionService.getAllMachineActions().size());
    }

    /**
     * 是最后一次，无法库存也无法后继成品。
     */
    @Test
    void test2() {
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "3", LocalDateTime.now(), 20000002, "2021050101", "1", "4×1500×3600", "对重架工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000002);
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.STRAIGHT_WEIGHT.getName()));
        straightModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order, OrderUtil.getFakeOrder());
        actionService.getAllMachineActions().forEach(System.out::println);
        // 长度修边-旋转-进刀余料-进刀2个成品-送成品:
        assertEquals(6, actionService.getAllMachineActions().size());
    }

    /**
     * 是最后一次，可以后继成品。
     */
    @Test
    void test3() {
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "3", LocalDateTime.now(), 20000003, "2021050101", "1", "4×1500×3600", "对重架工地模块", "0"));
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3100", "热板", "3", LocalDateTime.now(), 20000033, "2021050101", "1", "4×1500×3600", "对重架工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000003);
        WorkOrder nextOrder = orderService.getOrderById(20000033);
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.STRAIGHT_WEIGHT.getName()));
        straightModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order, nextOrder);
        actionService.getAllMachineActions().forEach(System.out::println);
        // 长度修边-旋转-进刀废料-进刀3个成品-旋转-长度修边-旋转-进刀2个成品-送板:
        assertEquals(12, actionService.getAllMachineActions().size());
    }

    /**
     * 是最后一次，无法后继成品，可以库存。
     */
    @Test
    void test4() {
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "3", LocalDateTime.now(), 20000004, "2021050101", "1", "4×1500×3600", "对重架工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000004);
        NormalBoard product = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT, order.getId());
        stockSpecService.insertStockSpec(product.getHeight(), product.getWidth(), new BigDecimal(3180));
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.STRAIGHT_WEIGHT.getName()));
        straightModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order, OrderUtil.getFakeOrder());
        actionService.getAllMachineActions().forEach(System.out::println);
        // 长度修边-旋转-进刀废料-进刀3个成品-旋转-长度修边-旋转-进刀2个库存件-送库存件:
        assertEquals(12, actionService.getAllMachineActions().size());
    }

    /**
     * 是最后一次，无法后继成品，可以库存，库存先。
     */
    @Test
    void test5() {
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "3", LocalDateTime.now(), 20000005, "2021050101", "1", "4×1500×3600", "对重架工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000005);
        NormalBoard product = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT, order.getId());
        stockSpecService.insertStockSpec(product.getHeight(), product.getWidth(), new BigDecimal(3200));
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.STRAIGHT_WEIGHT.getName()));
        straightModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order, OrderUtil.getFakeOrder());
        actionService.getAllMachineActions().forEach(System.out::println);
        // 长度修边-旋转-进刀废料-进刀3个库存件-旋转-长度修边-旋转-进刀2个成品-送成品:
        assertEquals(12, actionService.getAllMachineActions().size());
    }

    /**
     * 不是最后一次，从前向后排板。
     */
    @Test
    void test6() {
        orderService.insertOrder(new WorkOrder("未开工", "4×135×3190", "热板", "10", LocalDateTime.now(), 20000006, "2021050101", "1", "4×1500×3600", "对重架工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000006);
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.STRAIGHT_WEIGHT.getName()));
        straightModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order, OrderUtil.getFakeOrder());
        actionService.getAllMachineActions().forEach(System.out::println);
        // 长度修边-旋转-进刀废料-进刀9个成品-送余料:
        assertEquals(13, actionService.getAllMachineActions().size());
    }

    /**
     * 是最后一次，无法后继成品，可以库存件，成品宽度小于深度且小于总宽。
     */
    @Test
    void test7() {
        orderService.insertOrder(new WorkOrder("未开工", "4×235×3190", "热板", "2", LocalDateTime.now(), 20000007, "2021050101", "1", "4×1500×3600", "对重架工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000007);
        NormalBoard product = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT, order.getId());
        stockSpecService.insertStockSpec(product.getHeight(), new BigDecimal(245), new BigDecimal(3200));
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.STRAIGHT_WEIGHT.getName()));
        straightModuleServiceImpl.processOrder(parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order), order, OrderUtil.getFakeOrder());
        actionService.getAllMachineActions().forEach(System.out::println);
        // 长度修边-旋转-进刀废料-进刀3个库存件-旋转-长度修边-旋转-进刀2个成品-送余料:
        assertEquals(12, actionService.getAllMachineActions().size());
    }
}
