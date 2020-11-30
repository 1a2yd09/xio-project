package com.cat;

import com.cat.entity.bean.WorkOrder;
import com.cat.entity.signal.CuttingSignal;
import com.cat.service.ActionService;
import com.cat.service.MainService;
import com.cat.service.OrderService;
import com.cat.service.ParameterService;
import com.cat.utils.SignalUtils;
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
        // 半成品固定宽度192，(1250-121*2)/192=5个半成品，1250-192*5=290，290-121*2=48
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtils.getDefaultCuttingSignal(order));
        // 旋转-裁剪半成品(5个)-旋转-裁剪长度(2504->2185)-旋转-裁剪宽度(290->242)-裁剪成品(1个)-送成品
        assertEquals(12, actionService.getMachineActionCount());
    }

    /**
     * 成品规格不符合标准，即成品规格宽度是大于长度的。
     */
    @Test
    void test2() {
        // 下料板:2.5×1250×1589，成品板:2.5×1345.5×1189，需求1个成品板
        WorkOrder order = orderService.getOrderById(3098575);
        // 半成品固定宽度192，(1250-1189)/192=0个半成品，1250-1189=61，1589-1345.5=243.5
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtils.getDefaultCuttingSignal(order));
        this.actionService.getAllMachineActions().forEach(System.out::println);
        // 裁剪长度(1589->1345.5)-旋转-裁剪宽度(1250->1189)-送成品
        assertEquals(4, actionService.getMachineActionCount());
    }
}
