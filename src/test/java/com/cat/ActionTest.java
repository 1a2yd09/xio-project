package com.cat;

import com.cat.enums.ActionState;
import com.cat.enums.BoardCategory;
import com.cat.enums.OrderState;
import com.cat.pojo.Inventory;
import com.cat.pojo.MachineAction;
import com.cat.pojo.NormalBoard;
import com.cat.pojo.WorkOrder;
import com.cat.service.*;
import com.cat.service.impl.BottomModuleServiceImpl;
import com.cat.service.impl.StraightModuleServiceImpl;
import com.cat.utils.OrderUtil;
import com.cat.utils.SignalUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActionTest extends BaseTest {
    @Autowired
    ActionService actionService;
    @Autowired
    OrderService orderService;
    @Autowired
    MainService mainService;
    @Autowired
    InventoryService inventoryService;
    @Autowired
    StockSpecService stockSpecService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    BottomModuleServiceImpl bottomModuleServiceImpl;
    @Autowired
    StraightModuleServiceImpl straightModuleServiceImpl;

    @Test
    void testNotBottomAction() {
        // 经过下述对重流程，将生成3个库存件和2个成品，工单本身需求2个成品:
        // 下料板: 4.00×1245.00×3400.00
        // 成品板: 4.0×245×3190
        // 库存件: 4.0×245×3300
        WorkOrder order = orderService.getOrderById(3098562);
        // 生成机器动作前，先写入库存件信息:
        NormalBoard stock = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.STOCK, order.getId());
        stock.setLength(new BigDecimal(3300));
        stockSpecService.insertStockSpec(stock.getHeight(), stock.getWidth(), stock.getLength());
        straightModuleServiceImpl.processOrder(order, OrderUtil.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), SignalUtil.getDefaultCuttingSignal(order));
        // 测试一，生成11个机器动作:
        assertEquals(11, actionService.getMachineActionCount());

        // 处理完所有动作前，获取工单原来的未完成数目和指定半成品的数目:
        int oldUnfinishedCount = order.getIncompleteQuantity();
        Inventory inventory = inventoryService.getInventory(stock.getStandardSpec(), stock.getMaterial(), stock.getCategory().value);
        int oldFinishedCount = inventory == null ? 0 : inventory.getQuantity();
        actionService.processCompletedAction(order);

        order = this.orderService.getCompletedOrderById(3098562);
        int newUnfinishedCount = order.getIncompleteQuantity();
        // 测试二，工单的未完成数目等于原来的未完成数目减去上面生成的成品数目:
        assertEquals(newUnfinishedCount, oldUnfinishedCount - 2);
        // 测试三，达到了工单所需的数目，因此工单状态应为已完工:
        assertEquals(order.getOperationState(), OrderState.COMPLETED.value);

        inventory = inventoryService.getInventory(stock.getStandardSpec(), stock.getMaterial(), stock.getCategory().value);
        int newFinishedCount = inventory == null ? 0 : inventory.getQuantity();
        // 测试四，该库存件的数目等于原来的数目加上上面生成的库存件数目:
        assertEquals(newFinishedCount, oldFinishedCount + 3);
    }

    @Test
    void testBottomOrderAction() {
        // 经过下述轿底流程，将生成3个半成品和2个成品，工单本身需求2个成品:
        // 下料板 2.5×1250×2504:
        // 成品板 2.5×121×2185:
        // 半成品 2.5×192×2504:
        WorkOrder order = orderService.getOrderById(3099510);

        bottomModuleServiceImpl.processOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        // 测试一，生成10个机器动作:
        assertEquals(10, actionService.getMachineActionCount());

        List<MachineAction> actions = actionService.getAllMachineActions();
        actions.forEach(System.out::println);
        for (MachineAction action : actions) {
            // 测试二，所有动作刚生成时都是未完成:
            assertEquals(ActionState.INCOMPLETE.value, action.getState());
        }

        actionService.completedAllMachineActions();
        actions = actionService.getAllMachineActions();
        for (MachineAction action : actions) {
            // 测试三，所有动作都被置为已完成:
            assertEquals(ActionState.COMPLETED.value, action.getState());
        }

        // 处理完所有动作前，获取工单原来的未完成数目和指定半成品的数目:
        int oldUnfinishedCount = order.getIncompleteQuantity();
        NormalBoard semiProduct = new NormalBoard("2.50×192.00×2504.00", "镀锌板", BoardCategory.SEMI_PRODUCT, order.getId());
        Inventory inventory = inventoryService.getInventory(semiProduct.getStandardSpec(), semiProduct.getMaterial(), semiProduct.getCategory().value);
        int oldFinishedCount = inventory == null ? 0 : inventory.getQuantity();
        actionService.processCompletedAction(order);

        order = this.orderService.getCompletedOrderById(3099510);
        int newUnfinishedCount = order.getIncompleteQuantity();
        // 测试四，工单的未完成数目等于原来的未完成数目减去上面生成的成品数目:
        assertEquals(newUnfinishedCount, oldUnfinishedCount - 2);
        // 测试五，达到了工单的需求量，因此工单状态应该为”已完成“:
        assertEquals(order.getOperationState(), OrderState.COMPLETED.value);

        inventory = inventoryService.getInventory(semiProduct.getStandardSpec(), semiProduct.getMaterial(), semiProduct.getCategory().value);
        int newFinishedCount = inventory == null ? 0 : inventory.getQuantity();
        // 测试四，该半成品的数目等于原来的数目加上上面生成的半成品数目:
        assertEquals(newFinishedCount, oldFinishedCount + 3);
    }

    @Test
    void testCompletedAllActions() {
        WorkOrder order = orderService.getOrderById(3098528);
        bottomModuleServiceImpl.processOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        assertFalse(actionService.isAllMachineActionsProcessed());
        actionService.completedMachineActionById(1);
        assertFalse(actionService.isAllMachineActionsProcessed());
        actionService.completedAllMachineActions();
        assertTrue(actionService.isAllMachineActionsProcessed());
    }
}
