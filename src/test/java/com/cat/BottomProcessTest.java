package com.cat;

import com.cat.pojo.WorkOrder;
import com.cat.service.ActionService;
import com.cat.service.MainService;
import com.cat.service.OrderService;
import com.cat.service.ParameterService;
import com.cat.utils.SignalUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@Rollback
class BottomProcessTest extends BaseTest {
    @Autowired
    MainService mainService;
    @Autowired
    OrderService orderService;
    @Autowired
    ActionService actionService;
    @Autowired
    ParameterService parameterService;

    /**
     * 成品规格符合标准，即成品规格宽度是小于长度的。
     */
    @Test
    void test1() {
        // 下料板:2.5×1250×2504，成品板:2.5×121×2185，需求2个成品板
        WorkOrder order = orderService.getOrderById(3099510);
        // 由于成品总宽度小于夹钳宽度且需要修剪长度，因此将成品总宽度补齐至900
        // 半成品固定宽度192，(1250-900)/192=1个半成品，1250-192*1=1058，1058-121*2=816
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 旋转-裁剪半成品(1个)-旋转-裁剪长度(2504->2185)-旋转-裁剪成品(2个)-送余料
        assertEquals(8, actionService.getMachineActionCount());
    }

    /**
     * 成品规格符合标准，即成品规格宽度是小于长度的。
     */
    @Test
    void test11() {
        // 下料板:2.5×1250×2504，成品板:2.5×121×2185，需求2个成品板
        WorkOrder order = orderService.getOrderById(3099510);
        // 由于成品总宽度小于夹钳宽度且需要修剪长度，因此将成品总宽度补齐至900
        // 半成品固定宽度192，(1250-900)/192=1个半成品，1250-192*1=1058，1058-121*2=816
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 旋转-裁剪半成品(1个)-旋转-裁剪长度(2504->2185)-旋转-裁剪成品(2个)-送余料
    }

    /**
     * 成品规格不符合标准，即成品规格宽度是大于长度的。
     */
    @Test
    void test2() {
        // 下料板:2.5×1250×1589，成品板:2.5×1345.5×1189，需求1个成品板
        WorkOrder order = orderService.getOrderById(3098575);
        // 半成品固定宽度192，(1250-1189)/192=0个半成品，1250-1189=61
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 裁剪长度(1589->1345.5)-旋转-裁剪成品(1250->61)-送废料
        assertEquals(4, actionService.getMachineActionCount());
    }

    /**
     * 成品规格符合标准，即成品规格宽度是小于长度的，但是没有补齐操作。
     */
    @Test
    void test3() {
        // 下料板:2.5×1250×2504，成品板:2.5×121×2185，需求2个成品板
        WorkOrder order = orderService.getOrderById(3099510);
        order.setProductSpecification("2.5×121×2504");
        // 虽然成品总宽度小于夹钳宽度但是不需要修剪长度，因此不需要将成品总宽度补齐至900
        // 半成品固定宽度192，(1250-242)/192=5个半成品，1250-192*5=290，290-121*2=48
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 旋转-裁剪半成品(5个)-裁剪成品-送废料-送成品
        assertEquals(9, actionService.getMachineActionCount());
    }

    @Test
    void test33() {
        // 下料板:2.5×1250×2504，成品板:2.5×121×2185，需求2个成品板
        WorkOrder order = orderService.getOrderById(3099510);
        order.setProductSpecification("2.5×121×2504");
        // 虽然成品总宽度小于夹钳宽度但是不需要修剪长度，因此不需要将成品总宽度补齐至900
        // 半成品固定宽度192，(1250-242)/192=5个半成品，1250-192*5=290，290-121*2=48
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 旋转-裁剪半成品(5个)-裁剪成品-送废料-送成品
    }

    /**
     * 成品规格符合标准，即成品规格宽度是小于长度的，有补齐操作，但是没有半成品生成。
     */
    @Test
    void test4() {
        // 下料板:2.5×1250×2504，成品板:2.5×121×2185，需求2个成品板
        WorkOrder order = orderService.getOrderById(3099510);
        order.setCuttingSize("2.5×1000×2504");
        // 虽然成品总宽度小于夹钳宽度但是不需要修剪长度，因此不需要将成品总宽度补齐至900
        // 半成品固定宽度192，(1000-900)/192=0个半成品
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 裁剪长度-旋转-裁剪成品-裁剪成品-送余料
        assertEquals(5, actionService.getMachineActionCount());
    }

    /**
     * 成品规格符合标准，即成品规格宽度是小于长度的，有补齐操作，但是没有半成品生成。
     */
    @Test
    void test44() {
        // 下料板:2.5×1250×2504，成品板:2.5×121×2185，需求2个成品板
        WorkOrder order = orderService.getOrderById(3099510);
        order.setProductQuantity("8");
        order.setCuttingSize("2.5×1000×2504");
        // 虽然成品总宽度小于夹钳宽度但是不需要修剪长度，因此不需要将成品总宽度补齐至900
        // 半成品固定宽度192，(1000-900)/192=0个半成品
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 裁剪长度-旋转-裁剪成品-裁剪成品-送余料
    }

    /**
     * 成品规格符合标准，即成品规格宽度是小于长度的，有补齐操作，但是没有半成品生成。
     */
    @Test
    void test55() {
        // 下料板:2.5×1250×2504，成品板:2.5×?×2185，需求?个成品板
        WorkOrder order = orderService.getOrderById(3099510);
        order.setProductQuantity("8");
        order.setProductSpecification("2.5×240×2185");
        // 虽然成品总宽度小于夹钳宽度但是不需要修剪长度，因此不需要将成品总宽度补齐至900
        // 半成品固定宽度192，(1000-900)/192=0个半成品
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 裁剪长度-旋转-裁剪成品-裁剪成品-送余料
    }

    @Disabled("TODO")
    @Test
    void testBottomProductCanNotCut1() {
        WorkOrder order = orderService.getOrderById(3098528);
        order.setCuttingSize("2.5×900×1100");
        order.setProductSpecification("2.5×1000×1000");
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
        // 旋转-裁剪半成品(4个)-送余料
        assertEquals(6, actionService.getMachineActionCount());
    }
}
