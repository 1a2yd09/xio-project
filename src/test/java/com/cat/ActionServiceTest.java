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
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void testProcessingFinishedAction1() {
        // 经过下述直梁流程，将生成2个库存件和2个成品，工单本身需求2个成品:
        // 下料板 4.0×1000×3400:
        // 成品板 4.0×245×3190:
        // 库存件 4.0×245×3300:

        WorkOrder order = workOrderService.getOrderById(3098562);
        order.setCuttingSize("4.0×1000×3400");
        Board stock = new Board(order.getSpecification(), order.getMaterial(), BoardCategory.STOCK);
        stock.setLength(new BigDecimal(3300));
        stockSpecificationService.addStockSpecification(stock.getHeight(), stock.getWidth(), stock.getLength());

        CutBoard legacyCutBoard = mainService.processingNotBottomOrder(order, null, null);
        // 取板-修边(无)-修长度(3400->3300)-旋转-进刀2个库存(1000->510)-旋转-修长度(3300->3190)-旋转-修宽度(510->490)-进刀1个成品(490->245)-送1个成品(245->0):
        // 测试一，生成11个机器动作:
        assertEquals(11, machineActionService.getActionCount());
        // 测试二，没有剩余板材:
        assertNull(legacyCutBoard);

        int oldUnfinishedCount = order.getUnfinishedAmount();
        Inventory inventory = inventoryService.getInventory(stock.getSpecification(), stock.getMaterial(), stock.getCategory().value);
        int oldFinishedCount = inventory == null ? 0 : inventory.getAmount();

        machineActionService.processCompletedAction(order);

        int newUnfinishedCount = order.getUnfinishedAmount();
        // 测试三，工单的未完成数目等于原来的未完成数目减去上面生成的成品数目:
        assertEquals(newUnfinishedCount, oldUnfinishedCount - 2);
        // 测试四，达到了工单所需的数目，因此工单状态应为已完工:
        assertEquals(order.getOperationState(), OrderState.COMPLETED.value);

        inventory = inventoryService.getInventory(stock.getSpecification(), stock.getMaterial(), stock.getCategory().value);
        int newFinishedCount = inventory == null ? 0 : inventory.getAmount();
        // 测试五，该库存件的数目等于原来的数目加上上面生成的库存件数目:
        assertEquals(newFinishedCount, oldFinishedCount + 2);
    }

    @Test
    void testProcessingFinishedAction2() {
        // 经过下述轿底流程，将生成5个半成品和2个成品，工单本身需求2个成品:
        // 下料板 2.5×1250×2504:
        // 成品板 2.5×121×2185:
        // 半成品 2.5×192×2504:

        WorkOrder order = workOrderService.getOrderById(3099510);
        Board semiProduct = new Board("2.50×192.00×2504.00", "镀锌板", BoardCategory.SEMI_PRODUCT);

        mainService.processingBottomOrder(order);
        // 取板-修边(无)-旋转-进刀5个半成品(1250->290)-旋转-修长度(2504->2185)-旋转-修宽度(290->242)-进刀1个成品(242->121)-送1个成品(121->0):
        // 测试一，生成13个机器动作:
        assertEquals(13, machineActionService.getActionCount());

        int oldUnfinishedCount = order.getUnfinishedAmount();
        Inventory inventory = inventoryService.getInventory(semiProduct.getSpecification(), semiProduct.getMaterial(), semiProduct.getCategory().value);
        int oldFinishedCount = inventory == null ? 0 : inventory.getAmount();

        machineActionService.processCompletedAction(order);

        int newUnfinishedCount = order.getUnfinishedAmount();
        // 测试二，工单的未完成数目等于原来的未完成数目减去上面生成的成品数目:
        assertEquals(newUnfinishedCount, oldUnfinishedCount - 2);
        // 测试三，达到了工单所需的数目，因此工单状态应为已完工:
        assertEquals(order.getOperationState(), OrderState.COMPLETED.value);

        inventory = inventoryService.getInventory(semiProduct.getSpecification(), semiProduct.getMaterial(), semiProduct.getCategory().value);
        int newFinishedCount = inventory == null ? 0 : inventory.getAmount();
        // 测试四，该半成品的数目等于原来的数目加上上面生成的半成品数目:
        assertEquals(newFinishedCount, oldFinishedCount + 5);
    }
}
