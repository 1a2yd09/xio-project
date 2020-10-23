package com.cat;

import com.cat.entity.Board;
import com.cat.entity.CutBoard;
import com.cat.entity.Inventory;
import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BoardCategory;
import com.cat.entity.enums.OrderState;
import com.cat.service.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionServiceTest {
    static ApplicationContext context;
    static MachineActionService machineActionService;
    static WorkOrderService workOrderService;
    static MainService mainService;
    static InventoryService inventoryService;
    static StockSpecificationService stockSpecificationService;

    @BeforeAll
    public static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        machineActionService = context.getBean(MachineActionService.class);
        workOrderService = context.getBean(WorkOrderService.class);
        mainService = context.getBean(MainService.class);
        inventoryService = context.getBean(InventoryService.class);
        stockSpecificationService = context.getBean(StockSpecificationService.class);
    }

    /**
     * 测试处理一组被机器完成的动作语句，
     * 处理后的工单未完成数目=处理前的工单未完成数目-生成的成品语句数目
     * 处理后的存货表中某规格、材质的库存件数目=处理前的存货表中某规格、材质的库存件数目+生成的库存件语句数目
     */
    @Test
    void testProcessingFinishedAction1() {
        // 经过下述直梁流程，将生成2个库存件和2个成品，工单本身需求2个成品:
        machineActionService.clearAllAction();
        WorkOrder order = workOrderService.getOrderById(3098562);
        order.setCuttingSize("4.0×1000×3400");
        // 库存件信息:
        Board board = new Board(order.getSpecification(), order.getMaterial(), BoardCategory.STOCK);
        board.setLength(new BigDecimal(3300));
        stockSpecificationService.addStockSpecification(board.getHeight(), board.getWidth(), board.getLength());
        CutBoard cutBoard = mainService.processingNotBottomOrder(order, null, null);
        assertEquals(11, machineActionService.getActionCount());
        // 库存件信息:
        // 获取处理前的工单未完成数目:
        String oldCompletedAmount = order.getCompletedAmount();
        int oldUnfinishedCount = order.getUnfinishedAmount();
        // 获取处理前的该规格及材质的库存件数目:
        Inventory inventory = inventoryService.getInventory(board.getSpecification(), board.getMaterial(), board.getCategory().value);
        // 如果没有这个规格和材质的库存件，那原数目就为零:
        int oldFinishedCount = inventory == null ? 0 : inventory.getAmount();

        // 处理动作:
        machineActionService.processingFinishedAction();

        // 获取处理后的工单未完成数目:
        order = workOrderService.getOrderById(3098562);
        int newUnfinishedCount = order.getUnfinishedAmount();
        // 判断:
        assertEquals(newUnfinishedCount, oldUnfinishedCount - 2);

        // 获取处理后的库存件数目:
        inventory = inventoryService.getInventory(board.getSpecification(), board.getMaterial(), board.getCategory().value);
        int newFinishedCount = inventory == null ? 0 : inventory.getAmount();
        // 判断:
        assertEquals(newFinishedCount, oldFinishedCount + 2);

        // 成品数目达到了工单需求量，因此工单状态应为已完工:
        assertEquals(order.getOperationState(), OrderState.COMPLETED.value);

        // 复原工单数目和工单状态:
        order.setCompletedAmount(oldCompletedAmount);
        workOrderService.updateOrderCompletedAmount(order.getCompletedAmount(), order.getId());
        order.setOperationState(OrderState.NOT_YET_STARTED.value);
        workOrderService.updateOrderState(order.getOperationState(), order.getId());
    }

    /**
     * 测试处理一组被机器完成的动作语句，
     * 处理后的工单未完成数目=处理前的工单未完成数目-生成的成品语句数目
     * 处理后的存货表中某规格、材质的半成品数目=处理前的存货表中某规格、材质的半成品数目+生成的半成品语句数目
     */
    @Test
    void testProcessingFinishedAction2() {
        // 经过下述轿底流程，将生成5个半成品和2个成品，工单本身需求2个成品:
        machineActionService.clearAllAction();
        WorkOrder order = workOrderService.getOrderById(3099510);
        mainService.processingBottomOrder(order);
        assertEquals(13, machineActionService.getActionCount());
        // 半成品信息:
        Board semiProduct = new Board("2.50×192.00×2504.00", "镀锌板", BoardCategory.SEMI_PRODUCT);
        // 获取处理前的工单未完成数目:
        String oldCompletedAmount = order.getCompletedAmount();
        int oldUnfinishedCount = order.getUnfinishedAmount();
        // 获取处理前的该规格及材质的半成品数目:
        Inventory inventory = inventoryService.getInventory(semiProduct.getSpecification(), semiProduct.getMaterial(), semiProduct.getCategory().value);
        // 如果没有这个规格和材质的半成品，那原数目就为零:
        int oldFinishedCount = inventory == null ? 0 : inventory.getAmount();

        // 处理动作:
        machineActionService.processingFinishedAction();

        // 获取处理后的工单未完成数目:
        order = workOrderService.getOrderById(3099510);
        int newUnfinishedCount = order.getUnfinishedAmount();
        // 判断:
        assertEquals(newUnfinishedCount, oldUnfinishedCount - 2);

        // 获取处理后的半成品数目:
        inventory = inventoryService.getInventory(semiProduct.getSpecification(), semiProduct.getMaterial(), semiProduct.getCategory().value);
        int newFinishedCount = inventory == null ? 0 : inventory.getAmount();
        // 判断:
        assertEquals(newFinishedCount, oldFinishedCount + 5);

        // 成品数目达到了工单需求量，因此工单状态应为已完工:
        assertEquals(order.getOperationState(), OrderState.COMPLETED.value);

        // 复原工单数目:
        order.setCompletedAmount(oldCompletedAmount);
        workOrderService.updateOrderCompletedAmount(order.getCompletedAmount(), order.getId());
        order.setOperationState(OrderState.NOT_YET_STARTED.value);
        workOrderService.updateOrderState(order.getOperationState(), order.getId());
    }
}
