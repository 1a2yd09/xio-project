package com.cat;

import com.cat.enums.BoardCategory;
import com.cat.enums.OrderModule;
import com.cat.enums.OrderSortPattern;
import com.cat.enums.OrderState;
import com.cat.pojo.Inventory;
import com.cat.pojo.NormalBoard;
import com.cat.pojo.OperatingParameter;
import com.cat.pojo.WorkOrder;
import com.cat.service.*;
import com.cat.service.impl.BottomModuleServiceImpl;
import com.cat.service.impl.StraightModuleServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    /**
     * 测试直梁流程：生成3个成品和3个库存件，工单要求3个成品。
     */
    @Test
    void testStraight() {
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "3", LocalDateTime.now(), 20000005, "2021050101", "1", "4×1500×3600", "对重架工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000005);
        // 先写入库存件信息:
        NormalBoard stock = new NormalBoard(order.getProductSpecification(), order.getMaterial(), BoardCategory.STOCK, order.getId());
        stock.setLength(new BigDecimal(3200));
        stockSpecService.insertStockSpec(stock.getHeight(), stock.getWidth(), stock.getLength());
        // 处理完所有动作前，获取工单原来的未完成数目和指定库存件的数目:
        int oldUnfinishedCount = order.getIncompleteQuantity();
        Inventory inventory = inventoryService.getInventory(stock.getStandardSpec(), stock.getMaterial(), stock.getCategory().value);
        int oldFinishedCount = inventory == null ? 0 : inventory.getQuantity();
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.STRAIGHT_WEIGHT.getName()));
        straightModuleServiceImpl.process();
        // 测试一，生成12个机器动作:
        assertEquals(12, actionService.getProcessedActionCount());
        // 测试二，工单的未完成数目等于原来的未完成数目减去上面生成的成品数目:
        order = this.orderService.getCompletedOrderById(20000005);
        int newUnfinishedCount = order.getIncompleteQuantity();
        assertEquals(newUnfinishedCount, oldUnfinishedCount - 3);
        // 测试三，达到了工单所需的数目，因此工单状态应为已完工:
        assertEquals(order.getOperationState(), OrderState.COMPLETED.value);
        // 测试四，该库存件的数目等于原来的数目加上上面生成的库存件数目:
        inventory = inventoryService.getInventory(stock.getStandardSpec(), stock.getMaterial(), stock.getCategory().value);
        int newFinishedCount = inventory == null ? 0 : inventory.getQuantity();
        assertEquals(newFinishedCount, oldFinishedCount + 3);
    }

    /**
     * 测试轿底流程：将生成3个半成品和3个成品，工单本身需求3个成品。
     */
    @Test
    void testBottom() {
        orderService.insertOrder(new WorkOrder("未开工", "4×245×3190", "热板", "3", LocalDateTime.now(), 20000001, "2021050101", "1", "4×1500×3600", "轿底吊顶工地模块", "0"));
        WorkOrder order = orderService.getOrderById(20000001);
        // 处理前，获取工单原来的未完成数目和指定半成品的数目:
        int oldUnfinishedCount = order.getIncompleteQuantity();
        NormalBoard semiProduct = new NormalBoard("4×192×3600", "热板", BoardCategory.SEMI_PRODUCT, order.getId());
        Inventory inventory = inventoryService.getInventory(semiProduct.getStandardSpec(), semiProduct.getMaterial(), semiProduct.getCategory().value);
        int oldFinishedCount = inventory == null ? 0 : inventory.getQuantity();
        parameterService.insertOperatingParameter(new OperatingParameter(LocalDate.now(), new BigDecimal("192"), new BigDecimal("100"), OrderSortPattern.SEQ.getName(), OrderModule.BOTTOM_PLATFORM.getName()));
        bottomModuleServiceImpl.process();
        // 测试一，生成11个机器动作:
        assertEquals(11, actionService.getProcessedActionCount());
        // 测试二，工单的未完成数目等于原来的未完成数目减去上面生成的成品数目:
        order = this.orderService.getCompletedOrderById(20000001);
        int newUnfinishedCount = order.getIncompleteQuantity();
        assertEquals(newUnfinishedCount, oldUnfinishedCount - 3);
        // 测试三，达到了工单的需求量，因此工单状态应该为”已完成“:
        assertEquals(order.getOperationState(), OrderState.COMPLETED.value);
        // 测试四，该半成品的数目等于原来的数目加上上面生成的半成品数目:
        inventory = inventoryService.getInventory(semiProduct.getStandardSpec(), semiProduct.getMaterial(), semiProduct.getCategory().value);
        int newFinishedCount = inventory == null ? 0 : inventory.getQuantity();
        assertEquals(newFinishedCount, oldFinishedCount + 3);
    }
}
