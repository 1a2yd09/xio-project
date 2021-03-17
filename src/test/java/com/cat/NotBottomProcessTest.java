package com.cat;

import com.cat.enums.BoardCategory;
import com.cat.pojo.NormalBoard;
import com.cat.pojo.WorkOrder;
import com.cat.service.*;
import com.cat.utils.OrderUtil;
import com.cat.utils.SignalUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NotBottomProcessTest extends BaseTest {
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

    @Test
    void test1() {
        assertTrue(true);
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("10");
        mainService.processingNotBottomOrder(order, OrderUtil.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    @Test
    void test2() {
        assertTrue(true);
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("1");
        WorkOrder nextOrder = orderService.getOrderById(3118526);
        nextOrder.setProductQuantity("4");
        mainService.processingNotBottomOrder(order, nextOrder, parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    @Test
    void test3() {
        assertTrue(true);
        WorkOrder order = orderService.getOrderById(3098562);
        order.setCuttingSize("4×1300×3500");
        order.setProductQuantity("3");
        order.setProductSpecification("4×245×3400");
        WorkOrder nextOrder = orderService.getOrderById(3118526);
        nextOrder.setProductSpecification("4×242×3300");
        nextOrder.setProductQuantity("2");
        mainService.processingNotBottomOrder(order, nextOrder, parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    @Test
    void test4() {
        assertTrue(true);
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("2");
        WorkOrder nextOrder = orderService.getOrderById(3118526);
        mainService.processingNotBottomOrder(order, nextOrder, parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    @Test
    void test5() {
        assertTrue(true);
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("1");
        NormalBoard product = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT, order.getId());
        product.setLength(new BigDecimal("3200"));
        stockSpecService.insertStockSpec(product.getHeight(), product.getWidth(), product.getLength());
        mainService.processingNotBottomOrder(order, OrderUtil.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    @Test
    void test6() {
        assertTrue(true);
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("1");
        NormalBoard product = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT, order.getId());
        stockSpecService.insertStockSpec(product.getHeight(), product.getWidth(), product.getLength());
        mainService.processingNotBottomOrder(order, OrderUtil.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
    }
}
