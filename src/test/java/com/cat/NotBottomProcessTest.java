package com.cat;

import com.cat.entity.bean.WorkOrder;
import com.cat.entity.board.NormalBoard;
import com.cat.enums.BoardCategory;
import com.cat.service.*;
import com.cat.utils.OrderUtils;
import com.cat.utils.SignalUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@Rollback
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


    /**
     * 不是最后一次
     */
    @Test
    void test1() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("10");
        // 该工单需求10个成品，但1次只能裁剪5个成品，因此不是最后一次:
        // 下料板: 4.00×1245.00×3400.00
        // 成品板: 4.0×245×3190
        mainService.processingNotBottomOrder(order, OrderUtils.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        // 裁剪长度(3400->3190)-旋转-裁剪成品(4个)-裁剪废料-送成品:
        actionService.getAllMachineActions().forEach(System.out::println);
        assertEquals(8, actionService.getMachineActionCount());
    }

    /**
     * 不是最后一次
     */
    @Test
    void test11() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("10");
        // 该工单需求10个成品，但1次只能裁剪5个成品，因此不是最后一次:
        // 下料板: 4.00×1245.00×3400.00
        // 成品板: 4.0×245×3190
        mainService.processingNotBottomOrder(order, OrderUtils.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        // 裁剪长度(3400->3190)-旋转-裁剪成品(4个)-裁剪废料-送成品:
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    /**
     * 是最后一次-剩余板材可复用于后续成品(材质相同、剩余宽度大于900且当前成品长度大于等于后续成品长度)
     */
    @Test
    void test2() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("1");
        // 下料板: 4.00×1245.00×3400.00
        // 成品板: 4.0×245×3190
        WorkOrder nextOrder = orderService.getOrderById(3118526);
        // 成品需求量: 2
        // 成品板: 4.0×245×3130
        mainService.processingNotBottomOrder(order, nextOrder, parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        // 裁剪长度-旋转-裁剪当前成品(1个)-旋转-裁剪长度-旋转-裁剪后续成品(2个)-送余料
        actionService.getAllMachineActions().forEach(System.out::println);
        assertEquals(9, actionService.getMachineActionCount());
    }

    /**
     * 是最后一次-剩余板材可复用于后续成品(材质相同、剩余宽度大于900且当前成品长度大于等于后续成品长度)
     */
    @Test
    void test22() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("1");
        // 下料板: 4.00×1245.00×3400.00
        // 成品板: 4.0×245×3190
        WorkOrder nextOrder = orderService.getOrderById(3118526);
        nextOrder.setProductQuantity("4");
        // 成品需求量: 2
        // 成品板: 4.0×245×3130
        mainService.processingNotBottomOrder(order, nextOrder, parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        // 裁剪长度-旋转-裁剪当前成品(1个)-旋转-裁剪长度-旋转-裁剪后续成品(2个)-送余料
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    /**
     * 是最后一次-剩余板材可复用于后续成品(材质相同、剩余宽度大于900且当前成品长度大于等于后续成品长度)
     */
    @Test
    void test222() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setCuttingSize("4×1300×3500");
        order.setProductQuantity("3");
        order.setProductSpecification("4×245×3400");
        // 下料板: 4.00×1245.00×3400.00
        // 成品板: 4.0×245×3190
        WorkOrder nextOrder = orderService.getOrderById(3118526);
        nextOrder.setProductSpecification("4×242×3300");
        nextOrder.setProductQuantity("2");
        // 成品需求量: 2
        // 成品板: 4.0×245×3130
        mainService.processingNotBottomOrder(order, nextOrder, parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        // 裁剪长度-旋转-裁剪当前成品(1个)-旋转-裁剪长度-旋转-裁剪后续成品(2个)-送余料
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    /**
     * 是最后一次-不可复用(材质不对)-不可库存件(规格表为空)
     */
    @Test
    void test3() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("1");
        // 下料板: 4.00×1245.00×3400.00
        // 成品板: 4.0×245×3190
        // 热板
        WorkOrder nextOrder = orderService.getOrderById(3118526);
        // 成品板: 4.0×245×3130
        // 替换后续工单的成品材质
        nextOrder.setMaterial("冷板");
        mainService.processingNotBottomOrder(order, nextOrder, parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        // 裁剪长度-旋转-裁剪成品-送余料:
        actionService.getAllMachineActions().forEach(System.out::println);
        assertEquals(4, actionService.getMachineActionCount());
    }

    /**
     * 是最后一次-不可复用(无后续工单)-不可库存件(规格表为空)
     */
    @Test
    void test4() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("1");
        // 下料板: 4.00×1245.00×3400.00
        // 成品板: 4.0×245×3190
        mainService.processingNotBottomOrder(order, OrderUtils.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        // 裁剪长度-旋转-裁剪-送余料:
        actionService.getAllMachineActions().forEach(System.out::println);
        assertEquals(4, actionService.getMachineActionCount());
    }

    /**
     * 是最后一次-不可复用(规格不对)-不可库存件(规格表为空)
     */
    @Test
    void test5() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("1");
        // 下料板: 4.00×1245.00×3400.00
        // 成品板: 4.0×245×3190
        WorkOrder nextOrder = orderService.getOrderById(3118526);
        // 成品板: 4.0×245×3130
        // 替换后续工单的成品规格
        nextOrder.setProductSpecification("4.0×245×3200");
        mainService.processingNotBottomOrder(order, nextOrder, parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 裁剪长度-旋转-裁宽度-送板:
        assertEquals(4, actionService.getMachineActionCount());
    }

    /**
     * 是最后一次-不可复用(用于修边的宽度不够)-不可库存件(规格表为空)
     */
    @Test
    void test55() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("2");
        // 下料板: 4.00×1245.00×3400.00
        // 成品板: 4.0×245×3190
        WorkOrder nextOrder = orderService.getOrderById(3118526);
        // 成品板: 4.0×245×3130
        // 替换后续工单的成品规格
        mainService.processingNotBottomOrder(order, nextOrder, parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 裁剪长度-旋转-裁宽度-送板:
    }

    /**
     * 是最后一次-不可复用(无后续工单)-能库存-库存先(由于成品总宽度小于900，因此对宽度进行补齐)
     */
    @Test
    void test6() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("1");
        // 下料板: 4.00×1245.00×3400.00
        // 成品板: 4.0×245×3190
        // 向库存规格表中写入一个比成品长度更长的库存件:
        NormalBoard product = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
        product.setLength(new BigDecimal("3200"));
        stockSpecService.insertStockSpec(product.getHeight(), product.getWidth(), product.getLength());
        mainService.processingNotBottomOrder(order, OrderUtils.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 裁剪长度-旋转-裁库存件(1个)-旋转-裁剪长度-旋转-裁剪成品-送余料:
        assertEquals(8, actionService.getMachineActionCount());
    }

    /**
     * 是最后一次-不可复用(无后续工单)-能库存-库存先(由于成品总宽度小于900，因此对宽度进行补齐)
     */
    @Test
    void test66() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("1");
        // 下料板: 4.00×1245.00×3400.00
        // 成品板: 4.0×245×3190
        // 向库存规格表中写入一个比成品长度更长的库存件:
        NormalBoard product = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
        product.setLength(new BigDecimal("3200"));
        stockSpecService.insertStockSpec(product.getHeight(), product.getWidth(), product.getLength());
        mainService.processingNotBottomOrder(order, OrderUtils.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 裁剪长度-旋转-裁库存件(1个)-旋转-裁剪长度-旋转-裁剪成品-送余料:
    }

    /**
     * 是最后一次-不可复用(无后续工单)-能库存-成品先
     */
    @Test
    void test7() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("1");
        // 下料板: 4.00×1245.00×3400.00
        // 成品板: 4.0×245×3190
        NormalBoard product = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
        // 向规格表中写入一个和成品规格一致的库存件:
        stockSpecService.insertStockSpec(product.getHeight(), product.getWidth(), product.getLength());
        mainService.processingNotBottomOrder(order, OrderUtils.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 裁剪长度-旋转-裁剪成品(1个)-裁剪库存件(3个)-裁剪废料-送库存:
        assertEquals(8, actionService.getMachineActionCount());
    }

    /**
     * 是最后一次-不可复用(无后续工单)-能库存-成品先
     */
    @Test
    void test77() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setProductQuantity("1");
        // 下料板: 4.00×1245.00×3400.00
        // 成品板: 4.0×245×3190
        NormalBoard product = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
        // 向规格表中写入一个和成品规格一致的库存件:
        stockSpecService.insertStockSpec(product.getHeight(), product.getWidth(), product.getLength());
        mainService.processingNotBottomOrder(order, OrderUtils.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 裁剪长度-旋转-裁剪成品(1个)-裁剪库存件(3个)-裁剪废料-送库存:
    }

    @Disabled("TODO")
    @Test
    void testNotBottomProductCanNotCut1() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setCuttingSize("2.5×400×400");
        order.setProductSpecification("2.5×500×500");
        stockSpecService.insertStockSpec(new BigDecimal("2.5"), new BigDecimal("180"), new BigDecimal("180"));
        mainService.processingNotBottomOrder(order, OrderUtils.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 长度-旋转-库存件-废料-库存件
        assertEquals(5, actionService.getMachineActionCount());
    }

    @Disabled("TODO")
    @Test
    void testNotBottomProductCanNotCut2() {
        WorkOrder order = orderService.getOrderById(3098562);
        order.setCuttingSize("2.5×150×150");
        order.setProductSpecification("2.5×500×500");
        stockSpecService.insertStockSpec(new BigDecimal("2.5"), new BigDecimal("180"), new BigDecimal("180"));
        mainService.processingNotBottomOrder(order, OrderUtils.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 旋转-送板
        assertEquals(2, actionService.getMachineActionCount());
    }
}
