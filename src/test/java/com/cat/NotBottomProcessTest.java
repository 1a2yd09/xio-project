package com.cat;

import com.cat.entity.CutBoard;
import com.cat.entity.NormalBoard;
import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BoardCategory;
import com.cat.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

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
    @Autowired
    TrimmingValueService trimmingValueService;


    /**
     * 有留板-能用-不是最后一次
     */
    @Test
    void test1() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 直接用工单下料板作为留板，肯定能用:
        CutBoard legacyBoard = new CutBoard(order.getCuttingSize(), order.getMaterial());
        // 留板因为是裁剪了成品之后才留下来的，设置其为长边朝前:
        legacyBoard.setForwardEdge(CutBoard.EdgeType.LONG);
        // 该工单需求2个成品，但1次只能裁剪1个成品，因此不是最后一次:
        CutBoard cutBoard = mainService.processingNotBottomOrder(order, legacyBoard, null, parameterService.getLatestOperatingParameter(), trimmingValueService.getLatestTrimmingValue(), stockSpecService.getGroupSpecs());
        // 旋转-裁剪长度-送板:
        assertEquals(3, actionService.getActionCount());
        assertNull(cutBoard);
    }

    /**
     * 有留板-不能用-不是最后一次
     */
    @Test
    void test2() {
        WorkOrder order = orderService.getOrderById(3098562);
        CutBoard legacyBoard = new CutBoard(order.getCuttingSize(), order.getMaterial());
        legacyBoard.setForwardEdge(CutBoard.EdgeType.LONG);
        // 将留板改成不能用:
        legacyBoard.setWidth(BigDecimal.ZERO);
        // 该工单需求2个成品，但1次只能裁剪1个成品，因此不是最后一次:
        CutBoard cutBoard = mainService.processingNotBottomOrder(order, legacyBoard, null, parameterService.getLatestOperatingParameter(), trimmingValueService.getLatestTrimmingValue(), stockSpecService.getGroupSpecs());
        // 送板-取板-修边(无)-裁剪长度-送板:
        assertEquals(4, actionService.getActionCount());
        assertNull(cutBoard);
    }

    /**
     * 无留板-不是最后一次
     */
    @Test
    void test3() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 该工单需求2个成品，但1次只能裁剪1个成品，因此不是最后一次:
        CutBoard cutBoard = mainService.processingNotBottomOrder(order, null, null, parameterService.getLatestOperatingParameter(), trimmingValueService.getLatestTrimmingValue(), stockSpecService.getGroupSpecs());
        // 取板-修边(无)-裁剪长度-送板:
        assertEquals(3, actionService.getActionCount());
        assertNull(cutBoard);
    }

    /**
     * 无留板-是最后一次-剩的能用
     */
    @Test
    void test4() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 直接用工单本身成品板作为下一工单的成品板:
        NormalBoard nextOrderProductBoard = new NormalBoard(order.getSpecStr(), order.getMaterial(), BoardCategory.PRODUCT);
        // 将工单下料板的宽度改为原来的两倍多一点:
        order.setCuttingSize("4.0×500×3400");
        // 因为只需1个成品板，因此是最后一次，并且剩下的肯定能给后面的用:
        order.setAmount("1");
        CutBoard cutBoard = mainService.processingNotBottomOrder(order, null, nextOrderProductBoard, parameterService.getLatestOperatingParameter(), trimmingValueService.getLatestTrimmingValue(), stockSpecService.getGroupSpecs());
        // 取板-修边(无)-裁剪长度-旋转-裁成品(1个):
        assertEquals(4, actionService.getActionCount());
        assertNotNull(cutBoard);
        System.out.println(cutBoard);
    }

    /**
     * 无留板-是最后一次-剩的不能用-不能库存
     */
    @Test
    void test5() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 直接用工单本身成品板作为下一工单的成品板:
        NormalBoard nextOrderProductBoard = new NormalBoard(order.getSpecStr(), order.getMaterial(), BoardCategory.PRODUCT);
        // 材质不同，剩的不能用:
        nextOrderProductBoard.setMaterial("冷板");
        // 将工单下料板的宽度改为原来的两倍多一点:
        order.setCuttingSize("4.0×500×3400");
        NormalBoard product = new NormalBoard(order.getSpecStr(), order.getMaterial(), BoardCategory.PRODUCT);
        // 向库存规格表中写入一个和成品规格相同的库存件:
        stockSpecService.addStockSpec(product.getHeight(), product.getWidth(), product.getLength());
        // 该工单需求的是2个成品板，500裁掉2个245剩10，无法裁剪245的库存件:
        CutBoard cutBoard = mainService.processingNotBottomOrder(order, null, nextOrderProductBoard, parameterService.getLatestOperatingParameter(), trimmingValueService.getLatestTrimmingValue(), stockSpecService.getGroupSpecs());
        // 取板-修边(无)-裁剪长度-旋转-裁剪宽度-裁剪成品(1个)-送成品(1个):
        assertEquals(6, actionService.getActionCount());
        assertNull(cutBoard);
    }

    /**
     * 无留板-是最后一次-剩的不能用-能库存-成品先
     */
    @Test
    void test6() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 直接用工单本身成品板作为下一工单的成品板:
        NormalBoard nextOrderProductBoard = new NormalBoard(order.getSpecStr(), order.getMaterial(), BoardCategory.PRODUCT);
        // 材质不同，剩的不能用:
        nextOrderProductBoard.setMaterial("冷板");
        // 将工单下料板的宽度改为原来的两倍多一点:
        order.setCuttingSize("4.0×500×3400");
        // 是最后一次
        order.setAmount("1");
        NormalBoard product = new NormalBoard(order.getSpecStr(), order.getMaterial(), BoardCategory.PRODUCT);
        product.setLength(new BigDecimal(3100));
        // 像库存规格数据表中写入一个厚度宽度和成品相同，但长度稍小的库存件规格:
        stockSpecService.addStockSpec(product.getHeight(), product.getWidth(), product.getLength());
        // 该工单需求的是1个成品板，500裁掉1个245剩255，可以裁剪1个245的库存件，并且成品优先:
        CutBoard cutBoard = mainService.processingNotBottomOrder(order, null, nextOrderProductBoard, parameterService.getLatestOperatingParameter(), trimmingValueService.getLatestTrimmingValue(), stockSpecService.getGroupSpecs());
        // 取板-修边(无)-裁剪长度(3400->3190)-旋转-裁剪成品(1个)-旋转-裁剪长度(3190->3100)-旋转-裁剪宽度(10)-送库存件(1个):
        assertEquals(9, actionService.getActionCount());
        assertNull(cutBoard);
    }

    /**
     * 无留板-是最后一次-剩的不能用-能库存-库存先
     */
    @Test
    void test7() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 直接用工单本身成品板作为下一工单的成品板:
        NormalBoard nextOrderProductBoard = new NormalBoard(order.getSpecStr(), order.getMaterial(), BoardCategory.PRODUCT);
        // 材质不同，剩的不能用:
        nextOrderProductBoard.setMaterial("冷板");
        // 将工单下料板的宽度改为原来的四倍多一点:
        order.setCuttingSize("4.0×1000×3400");
        NormalBoard product = new NormalBoard(order.getSpecStr(), order.getMaterial(), BoardCategory.PRODUCT);
        product.setLength(new BigDecimal(3300));
        // 像库存规格数据表中写入一个厚度宽度和成品相同，但长度稍大的库存件规格:
        stockSpecService.addStockSpec(product.getHeight(), product.getWidth(), product.getLength());
        // 该工单需求的是2个成品板，1000裁掉2个245剩510，可以裁剪2个245的库存件，并且库存件优先:
        CutBoard cutBoard = mainService.processingNotBottomOrder(order, null, nextOrderProductBoard, parameterService.getLatestOperatingParameter(), trimmingValueService.getLatestTrimmingValue(), stockSpecService.getGroupSpecs());
        // 取板-修边(无)-裁剪长度(3400->3300)-旋转-裁剪库存件(2个)-旋转-裁剪长度(3300->3190)-旋转-裁剪宽度-裁剪成品(1个)-送成品:
        assertEquals(11, actionService.getActionCount());
        assertNull(cutBoard);
    }

    /**
     * 有留板-能用-是最后一次-剩的能用
     */
    @Test
    void test8() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 直接用工单下料板作为留板，肯定能用:
        CutBoard legacyBoard = new CutBoard(order.getCuttingSize(), order.getMaterial());
        // 留板因为是裁剪了成品之后才留下来的，因此肯定是长边朝前:
        legacyBoard.setForwardEdge(CutBoard.EdgeType.LONG);
        legacyBoard.setWidth(new BigDecimal("500"));
        // 直接用工单本身成品板作为下一工单的成品板:
        NormalBoard nextOrderProductBoard = new NormalBoard(order.getSpecStr(), order.getMaterial(), BoardCategory.PRODUCT);
        // 该工单需求1个成品，因此是最后一次:
        order.setAmount("1");
        // 留板500的宽度裁掉一个245后剩255，还可以放得下一个成品板:
        CutBoard cutBoard = mainService.processingNotBottomOrder(order, legacyBoard, nextOrderProductBoard, parameterService.getLatestOperatingParameter(), trimmingValueService.getLatestTrimmingValue(), stockSpecService.getGroupSpecs());
        // 旋转-裁剪长度(3400->3190)-旋转-裁剪成品(500->255):
        assertEquals(4, actionService.getActionCount());
        assertNotNull(cutBoard);
        System.out.println(cutBoard);
    }

    /**
     * 有留板-能用-是最后一次-剩的不能用-不能库存
     */
    @Test
    void test9() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 直接用工单下料板作为留板，肯定能用:
        CutBoard legacyBoard = new CutBoard(order.getCuttingSize(), order.getMaterial());
        // 留板因为是裁剪了成品之后才留下来的，因此肯定是长边朝前:
        legacyBoard.setForwardEdge(CutBoard.EdgeType.LONG);
        legacyBoard.setWidth(new BigDecimal("250"));
        // 直接用工单本身成品板作为下一工单的成品板:
        NormalBoard nextOrderProductBoard = new NormalBoard(order.getSpecStr(), order.getMaterial(), BoardCategory.PRODUCT);
        // 该工单需求1个成品，因此是最后一次:
        order.setAmount("1");
        NormalBoard board = new NormalBoard(order.getSpecStr(), order.getMaterial(), BoardCategory.PRODUCT);
        // 像库存规格数据表中写入一个和成品规格相同的库存件规格:
        stockSpecService.addStockSpec(board.getHeight(), board.getWidth(), board.getLength());
        // 留板250的宽度裁掉一个245后剩5，无法复用也无法用于库存件:
        CutBoard cutBoard = mainService.processingNotBottomOrder(order, legacyBoard, nextOrderProductBoard, parameterService.getLatestOperatingParameter(), trimmingValueService.getLatestTrimmingValue(), stockSpecService.getGroupSpecs());
        // 旋转-裁剪长度(3400->3190)-旋转-裁剪宽度(250->245)-送成品:
        assertEquals(5, actionService.getActionCount());
        assertNull(cutBoard);
    }

    /**
     * 有留板-能用-是最后一次-剩的不能用-能库存-库存先
     */
    @Test
    void test10() {
        WorkOrder order = orderService.getOrderById(3098562);
        // 直接用工单下料板作为留板，肯定能用:
        CutBoard legacyBoard = new CutBoard(order.getCuttingSize(), order.getMaterial());
        // 留板因为是裁剪了成品之后才留下来的，因此肯定是长边朝前:
        legacyBoard.setForwardEdge(CutBoard.EdgeType.LONG);
        legacyBoard.setWidth(new BigDecimal("500"));
        // 直接用工单本身成品板作为下一工单的成品板:
        NormalBoard nextOrderProductBoard = new NormalBoard(order.getSpecStr(), order.getMaterial(), BoardCategory.PRODUCT);
        // 剩的不能用:
        nextOrderProductBoard.setMaterial("冷板");
        // 该工单需求1个成品，因此是最后一次:
        order.setAmount("1");
        NormalBoard board = new NormalBoard(order.getSpecStr(), order.getMaterial(), BoardCategory.PRODUCT);
        // 像库存规格数据表中写入一个厚度宽度和成品相同，但长度稍大的库存件规格:
        board.setLength(new BigDecimal("3300"));
        stockSpecService.addStockSpec(board.getHeight(), board.getWidth(), board.getLength());
        // 留板500的宽度裁掉一个245后剩255，可用于1个245的库存件:
        CutBoard cutBoard = mainService.processingNotBottomOrder(order, legacyBoard, nextOrderProductBoard, parameterService.getLatestOperatingParameter(), trimmingValueService.getLatestTrimmingValue(), stockSpecService.getGroupSpecs());
        // 旋转-裁剪长度(3400->3300)-旋转-裁库存件(1个)-旋转-裁剪长度(3300->3190)-旋转-裁剪宽度(255->245)-送成品:
        assertEquals(9, actionService.getActionCount());
        assertNull(cutBoard);
    }
}