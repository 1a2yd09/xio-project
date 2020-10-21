package com.cat;

import com.cat.entity.Board;
import com.cat.entity.Inventory;
import com.cat.entity.MachineAction;
import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BoardCategory;
import com.cat.service.InventoryService;
import com.cat.service.MachineActionService;
import com.cat.service.MainService;
import com.cat.service.WorkOrderService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActionServiceTest {
    static ApplicationContext context;
    static MachineActionService machineActionService;
    static WorkOrderService workOrderService;
    static MainService mainService;
    static InventoryService inventoryService;

    @BeforeAll
    public static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        machineActionService = context.getBean(MachineActionService.class);
        workOrderService = context.getBean(WorkOrderService.class);
        mainService = context.getBean(MainService.class);
        inventoryService = context.getBean(InventoryService.class);
    }

    @Test
    void testDoneAllAction() {
        machineActionService.doneAllAction();
        List<MachineAction> list = machineActionService.getAllActions();
        for (MachineAction ma : list) {
            assertEquals(ma.getCompleted(), true);
        }
    }

    /**
     * 测试处理一组被机器完成的动作语句，
     * 处理后的工单未完成数目=处理前的工单未完成数目-生成的成品语句数目
     * 处理后的存货表中某规格、材质的半成品数目=处理前的存货表中某规格、材质的半成品数目+生成的半成品语句数目
     * 处理后的存货表中某规格、材质的库存件数目=处理前的存货表中某规格、材质的库存件数目+生成的库存件语句数目
     */
    @Test
    void testProcessingFinishedAction() {
        // 经过下述轿底流程，将生成5个半成品和2个成品:
        machineActionService.clearAllAction();
        WorkOrder order = workOrderService.getWorkOrderById(3099510);
        mainService.processingBottomOrder(order);
        assertEquals(machineActionService.getActionCount(), 13);
        // 半成品信息:
        Board semiProduct = new Board("2.50×192.00×2504.00", "镀锌板", BoardCategory.SEMI_PRODUCT);
        // 获取处理前的工单未完成数目:
        int oldUnfinishedCount = order.getUnfinishedAmount();
        // 获取处理前的该规格及材质的半成品数目:
        Inventory inventory = inventoryService.getInventory(semiProduct.getSpecification(), semiProduct.getMaterial(), semiProduct.getCategory().value);
        // 如果没有这个规格和材质的半成品，那原数目就为零:
        int oldFinishedCount = inventory == null ? 0 : inventory.getAmount();

        // 处理动作:
        machineActionService.processingFinishedAction();

        // 获取处理后的工单未完成数目:
        order = workOrderService.getWorkOrderById(3099510);
        int newUnfinishedCount = order.getUnfinishedAmount();
        // 判断:
        assertEquals(newUnfinishedCount, oldUnfinishedCount - 2);

        // 获取处理后的半成品数目:
        inventory = inventoryService.getInventory(semiProduct.getSpecification(), semiProduct.getMaterial(), semiProduct.getCategory().value);
        int newFinishedCount = inventory == null ? 0 : inventory.getAmount();
        // 判断:
        assertEquals(newFinishedCount, oldFinishedCount + 5);
    }
}
