package com.cat;

import com.cat.entity.bean.WorkOrder;
import com.cat.entity.board.NormalBoard;
import com.cat.enums.BoardCategory;
import com.cat.service.*;
import com.cat.utils.OrderUtils;
import com.cat.utils.SignalUtils;
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
        // 该工单需求2个成品，但1次只能裁剪1个成品，因此不是最后一次:
        // 下料板: 4.0×245×3400
        // 成品板: 4.0×245×3190
        mainService.processingNotBottomOrder(order, OrderUtils.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        // 裁剪长度(3400->3190)-旋转-送板:
        assertEquals(3, actionService.getMachineActionCount());
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    /**
     * 是最后一次-可复用(规格、材质符合)
     */
    @Test
    void test2() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 下料板: 4.0×245×3400
        // 成品板: 4.0×245×3190
        // 热板
        // 替换当前工单的下料板规格，使其剩余宽度大于后续工单的宽度
        order.setCuttingSize("4.0×1000×3400");
        // 只需1个成品板，是最后一次:
        order.setProductQuantity("1");
        WorkOrder nextOrder = orderService.getOrderById(3118526);
        // 成品板: 4.0×245×3130
        // 热板
        mainService.processingNotBottomOrder(order, nextOrder, parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        // 裁剪长度-旋转-裁剪当前成品(1个)-旋转-裁剪长度-旋转-裁剪宽度-裁下一成品板(1个)-送下一成品板(1个):
        assertEquals(9, actionService.getMachineActionCount());
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    /**
     * 是最后一次-不可复用(材质不对)-不可库存件(规格表为空)
     */
    @Test
    void test3() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 下料板: 4.0×245×3400
        // 成品板: 4.0×245×3190
        // 热板
        // 替换当前工单的下料板规格，使其剩余宽度大于后续工单的宽度
        order.setCuttingSize("4.0×500×3400");
        // 因为只需1个成品板，因此是最后一次:
        order.setProductQuantity("1");
        WorkOrder nextOrder = orderService.getOrderById(3118526);
        // 成品板: 4.0×245×3130
        // 热板
        // 替换后续工单的成品材质
        nextOrder.setMaterial("冷板");
        // 虽然500裁掉1个245还剩255，还可以裁剪一个245，且长度大于，但是材质不符合:
        mainService.processingNotBottomOrder(order, nextOrder, parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        // 裁剪长度-旋转-裁剪宽度-送板:
        assertEquals(4, actionService.getMachineActionCount());
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    /**
     * 是最后一次-不可复用(无后续工单)-不可库存件(规格表为空)
     */
    @Test
    void test4() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 下料板: 4.0×245×3400
        // 成品板: 4.0×245×3190
        // 因为只需1个成品板，因此是最后一次:
        order.setProductQuantity("1");
        mainService.processingNotBottomOrder(order, OrderUtils.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        // 裁剪长度-旋转-送板:
        assertEquals(3, actionService.getMachineActionCount());
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    /**
     * 是最后一次-不可复用(规格不对)-不可库存件(规格表为空)
     */
    @Test
    void test5() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 下料板: 4.0×245×3400
        // 成品板: 4.0×245×3190
        // 替换当前工单的下料板规格:
        order.setCuttingSize("4.0×500×3400");
        // 是最后一次:
        order.setProductQuantity("1");
        WorkOrder nextOrder = orderService.getOrderById(3118526);
        // 成品板: 4.0×245×3130
        // 热板
        // 替换后续工单的成品规格
        nextOrder.setProductSpecification("4.0×245×3200");
        mainService.processingNotBottomOrder(order, nextOrder, parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 裁剪长度-旋转-裁宽度-送板:
        assertEquals(4, actionService.getMachineActionCount());
    }

    /**
     * 是最后一次-不可复用(无后续工单)-能库存-库存先
     */
    @Test
    void test6() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 下料板: 4.0×245×3400
        // 成品板: 4.0×245×3190
        // 修改工单下料板规格:
        order.setCuttingSize("4.0×1000×3400");
        NormalBoard product = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
        // 向库存规格表中写入一个比成品长度更长的库存件:
        product.setLength(new BigDecimal("3200"));
        stockSpecService.insertStockSpec(product.getHeight(), product.getWidth(), product.getLength());
        // 该工单需求的是2个成品板，1000裁掉2个245剩510，可以裁剪2个245的库存件:
        mainService.processingNotBottomOrder(order, OrderUtils.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 裁剪长度-旋转-裁库存件(2个)-旋转-裁剪长度-旋转-裁剪宽度-裁剪成品(1个)-送成品(1个):
        assertEquals(10, actionService.getMachineActionCount());
    }

    /**
     * 是最后一次-不可复用(无后续工单)-能库存-成品先
     */
    @Test
    void test7() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 将工单下料板的宽度改为原来的两倍多一点:
        order.setCuttingSize("4.0×500×3400");
        // 是最后一次
        order.setProductQuantity("1");
        NormalBoard product = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
        // 向规格表中写入一个和成品规格一致的库存件:
        stockSpecService.insertStockSpec(product.getHeight(), product.getWidth(), product.getLength());
        // 该工单需求的是1个成品板，500裁掉1个245剩255，可以裁剪1个245的库存件:
        mainService.processingNotBottomOrder(order, OrderUtils.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtils.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 裁剪长度(3400->3190)-旋转-裁剪成品(1个)-裁剪宽度(10)-送库存件(1个):
        assertEquals(5, actionService.getMachineActionCount());
    }
}
