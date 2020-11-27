package com.cat;

import com.cat.entity.bean.Inventory;
import com.cat.entity.bean.MachineAction;
import com.cat.entity.bean.WorkOrder;
import com.cat.entity.board.NormalBoard;
import com.cat.enums.ActionState;
import com.cat.enums.BoardCategory;
import com.cat.enums.ForwardEdge;
import com.cat.enums.OrderState;
import com.cat.service.*;
import com.cat.utils.OrderUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@Rollback
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

    @Test
    void testProcessingFinishedAction1() {
        // 经过下述直梁流程，将生成2个库存件和2个成品，工单本身需求2个成品:
        // 下料板 4.0×1000×3400:
        // 成品板 4.0×245×3190:
        // 库存件 4.0×245×3300:

        WorkOrder order = orderService.getOrderById(3098562);
        order.setCuttingSize("4.0×1000×3400");
        NormalBoard stock = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.STOCK);
        stock.setLength(new BigDecimal(3300));
        stockSpecService.insertStockSpec(stock.getHeight(), stock.getWidth(), stock.getLength());

        mainService.processingNotBottomOrder(order, OrderUtils.getFakeOrder(), parameterService.getLatestOperatingParameter(), stockSpecService.getGroupStockSpecs(), ForwardEdge.SHORT.code);
        // 修长度(3400->3300)-旋转-进刀2个库存(1000->510)-旋转-修长度(3300->3190)-旋转-修宽度(510->490)-进刀1个成品(490->245)-送1个成品(245->0):
        // 测试一，生成10个机器动作:
        assertEquals(10, actionService.getMachineActionCount());

        List<MachineAction> actions = actionService.getAllMachineActions();
        actions.forEach(System.out::println);
        for (MachineAction action : actions) {
            assertEquals(ActionState.INCOMPLETE.value, action.getState());
        }
        actionService.completedAllMachineActions();
        actions = actionService.getAllMachineActions();
        for (MachineAction action : actions) {
            assertEquals(ActionState.COMPLETED.value, action.getState());
        }

        int oldUnfinishedCount = order.getIncompleteQuantity();
        Inventory inventory = inventoryService.getInventory(stock.getStandardSpecStr(), stock.getMaterial(), stock.getCategory().value);
        int oldFinishedCount = inventory == null ? 0 : inventory.getQuantity();

        mainService.processCompletedAction(order, BoardCategory.STOCK);

        int newUnfinishedCount = order.getIncompleteQuantity();
        // 测试二，工单的未完成数目等于原来的未完成数目减去上面生成的成品数目:
        assertEquals(newUnfinishedCount, oldUnfinishedCount - 2);
        // 测试三，达到了工单所需的数目，因此工单状态应为已完工:
        assertEquals(order.getOperationState(), OrderState.COMPLETED.value);

        inventory = inventoryService.getInventory(stock.getStandardSpecStr(), stock.getMaterial(), stock.getCategory().value);
        int newFinishedCount = inventory == null ? 0 : inventory.getQuantity();
        // 测试四，该库存件的数目等于原来的数目加上上面生成的库存件数目:
        assertEquals(newFinishedCount, oldFinishedCount + 2);
    }

    @Test
    void testProcessingFinishedAction2() {
        // 经过下述轿底流程，将生成5个半成品和2个成品，工单本身需求2个成品:
        // 下料板 2.5×1250×2504:
        // 成品板 2.5×121×2185:
        // 半成品 2.5×192×2504:

        WorkOrder order = orderService.getOrderById(3099510);
        NormalBoard semiProduct = new NormalBoard("2.50×192.00×2504.00", "镀锌板", BoardCategory.SEMI_PRODUCT);

        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), ForwardEdge.SHORT.code);
        // 旋转-进刀5个半成品(1250->290)-旋转-修长度(2504->2185)-旋转-修宽度(290->242)-进刀1个成品(242->121)-送1个成品(121->0):
        // 测试一，生成12个机器动作:
        assertEquals(12, actionService.getMachineActionCount());

        List<MachineAction> actions = actionService.getAllMachineActions();
        actions.forEach(System.out::println);
        for (MachineAction action : actions) {
            assertEquals(ActionState.INCOMPLETE.value, action.getState());
        }
        actionService.completedAllMachineActions();
        actions = actionService.getAllMachineActions();
        for (MachineAction action : actions) {
            assertEquals(ActionState.COMPLETED.value, action.getState());
        }

        int oldUnfinishedCount = order.getIncompleteQuantity();
        Inventory inventory = inventoryService.getInventory(semiProduct.getStandardSpecStr(), semiProduct.getMaterial(), semiProduct.getCategory().value);
        int oldFinishedCount = inventory == null ? 0 : inventory.getQuantity();

        mainService.processCompletedAction(order, BoardCategory.SEMI_PRODUCT);

        int newUnfinishedCount = order.getIncompleteQuantity();
        // 测试二，工单的未完成数目等于原来的未完成数目减去上面生成的成品数目:
        assertEquals(newUnfinishedCount, oldUnfinishedCount - 2);
        // 测试三，达到了工单所需的数目，因此工单状态应为已完工:
        assertEquals(order.getOperationState(), OrderState.COMPLETED.value);

        inventory = inventoryService.getInventory(semiProduct.getStandardSpecStr(), semiProduct.getMaterial(), semiProduct.getCategory().value);
        int newFinishedCount = inventory == null ? 0 : inventory.getQuantity();
        // 测试四，该半成品的数目等于原来的数目加上上面生成的半成品数目:
        assertEquals(newFinishedCount, oldFinishedCount + 5);
    }

    @Test
    void testCompletedAllActions() {
        WorkOrder order = orderService.getOrderById(3101165);
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), ForwardEdge.SHORT.code);
        assertFalse(actionService.isAllMachineActionsProcessed());
        actionService.completedMachineActionById(1);
        assertFalse(actionService.isAllMachineActionsProcessed());
        actionService.completedAllMachineActions();
        assertTrue(actionService.isAllMachineActionsProcessed());
    }
}
