package com.cat;

import com.cat.entity.WorkOrder;
import com.cat.service.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BottomOrderTest {
    static ApplicationContext context;
    static MainService mainService;
    static WorkOrderService workOrderService;
    static MachineActionService machineActionService;
    static ParameterService parameterService;
    static TrimmingValueService trimmingValueService;

    @BeforeAll
    static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        mainService = context.getBean(MainService.class);
        workOrderService = context.getBean(WorkOrderService.class);
        machineActionService = context.getBean(MachineActionService.class);
        parameterService = context.getBean(ParameterService.class);
        trimmingValueService = context.getBean(TrimmingValueService.class);
    }

    /**
     * 成品规格符合标准，即成品规格宽度是小于长度的。
     */
    @Test
    void test1() {
        // 清空一下动作表:
        machineActionService.clearActionTable();
        // 下料板:2.5×1250×2504，成品板:2.5×121×2185，需求2个成品板
        WorkOrder order = workOrderService.getOrderById(3099510);
        // 半成品固定宽度192，(1250-121*2)/192=5个半成品，1250-192*5=290，290-121*2=48
        mainService.processingBottomOrder(order, null, parameterService.getLatestOperatingParameter(), trimmingValueService.getLatestTrimmingValue());
        // 取板-修边(无)-旋转-裁剪半成品(5个)-旋转-裁剪长度(2185->2504)-旋转-裁剪宽度(290->242)-裁剪成品(1个)-送成品
        assertEquals(13, machineActionService.getActionCount());
    }

    /**
     * 成品规格不符合标准，即成品规格宽度是大于长度的。
     */
    @Test
    void test2() {
        // 清空一下动作表:
        machineActionService.clearActionTable();
        // 下料板:2.5×1250×1589，成品板:2.5×1345.5×1189，需求1个成品板
        WorkOrder order = workOrderService.getOrderById(3098575);
        // 半成品固定宽度192，(1250-1189)/192=0个半成品，1250-1189=61，1589-1345.5=243.5
        mainService.processingBottomOrder(order, null, parameterService.getLatestOperatingParameter(), trimmingValueService.getLatestTrimmingValue());
        // 取板-修边(无)-裁剪长度(1589->1345.5)-旋转-裁剪宽度(1250->1189)-送成品
        assertEquals(5, machineActionService.getActionCount());
    }
}
