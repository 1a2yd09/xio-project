package com.cat;

import com.cat.entity.*;
import com.cat.entity.enums.BoardCategory;
import com.cat.service.*;
import com.cat.util.BoardUtil;
import com.cat.util.OrderUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppConfigTest {
    final Logger logger = LoggerFactory.getLogger(getClass());

    static ApplicationContext context;
    static SignalService signalService;
    static WorkOrderService workOrderService;
    static ParameterService parameterService;
    static MachineActionService machineActionService;
    static BoardService boardService;
    static MainService mainService;
    static InventoryService inventoryService;

    @BeforeAll
    public static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        signalService = context.getBean(SignalService.class);
        workOrderService = context.getBean(WorkOrderService.class);
        parameterService = context.getBean(ParameterService.class);
        machineActionService = context.getBean(MachineActionService.class);
        boardService = context.getBean(BoardService.class);
        mainService = context.getBean(MainService.class);
        inventoryService = context.getBean(InventoryService.class);
    }

    @Test
    public void testSomething() {

    }

    @Test
    public void testProcessBottomOrder1() {
        WorkOrder order = workOrderService.getWorkOrderById(3101334);
        machineActionService.clearAllAction();
        mainService.processingBottomOrder(order);
        assertEquals(machineActionService.getActionCount(), 13);
    }

    @Test
    public void testProcessBottomOrder2() {
        WorkOrder order = workOrderService.getWorkOrderById(3098967);
        machineActionService.clearAllAction();
        mainService.processingBottomOrder(order);
        assertEquals(machineActionService.getActionCount(), 5);
    }

    @Test
    public void testProcessBottomOrder3() {
        WorkOrder order = workOrderService.getWorkOrderById(3101166);
        machineActionService.clearAllAction();
        mainService.processingBottomOrder(order);
        assertEquals(machineActionService.getActionCount(), 3);
    }

    @Test
    public void testGetAllWidthBetterBottomOrder() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        List<WorkOrder> orders = workOrderService.getBottomOrders(op.getWorkOrderDate());
        assertEquals(orders.size(), 914);
        for (WorkOrder order : orders) {
//            CutBoard board = new CutBoard(order.getCuttingSize(), order.getMaterial(), BoardCategory.CUTTING, 0);
            Board board = new Board(order.getSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
            if (board.getWidth().compareTo(board.getLength()) >= 0) {
                System.out.println(board);
                BoardUtil.standardizingBoard(board);
                System.out.println(board);
            }
        }
    }

    /**
     * 如何测试处理一组完成的机器动作：
     * 因为成品语句会写入数据表，因此需要判断这组机器动作中生成了多少个成品板，还要计算原来要求的数量是多少，
     * 两者相减得到最后未完成的数量。
     * 因为修改到数据表，需要复原。
     * 因为半成品语句会写入数据表，因此需要计算生成了多少个这样的半成品，都是一致的，然后计算数据表中原来有多少，
     * 两者相加得到最后的个数。
     * 因为修改到数据表，需要复原。
     */
    @Test
    public void testProcessFinishedAction() {
        int orderId = 3101334;
        WorkOrder order = workOrderService.getWorkOrderById(orderId);
        // 获取工单要求的成品数量:
        int amount = OrderUtil.amountPropertyStrToInt(order.getAmount());
        // 清空动作表:
        machineActionService.clearAllAction();
        // 处理工单并生成机器语句:
        mainService.processingBottomOrder(order);
        List<MachineAction> actions = machineActionService.getAllActions();
        int productBoardAmount = 0;
        int semiProductBoardAmount = 0;
        Board semiProductBoard = null;
        // 分别计算这组机器语句生成了多少个成品板和半成品板:
        for (MachineAction action : actions) {
            if (action.getBoardCategory().equals(BoardCategory.PRODUCT.value)) {
                productBoardAmount++;
            } else if (action.getBoardCategory().equals(BoardCategory.SEMI_PRODUCT.value)) {
                semiProductBoard = new Board(action.getBoardSpecification(), action.getBoardMaterial(), BoardCategory.SEMI_PRODUCT);
                semiProductBoardAmount++;
            }
        }
        int oldSemiProductBoardAmount = 0;
        if (semiProductBoard != null) {
            // 如果生成了半成品，则到存货表中查看是否已有对应的数据并获取已有的数量:
            Inventory inventory = inventoryService.getInventory(semiProductBoard.getSpecification(), semiProductBoard.getMaterial(), semiProductBoard.getCategory().value);
            if (inventory != null) {
                oldSemiProductBoardAmount = inventory.getAmount();
            }
        }
        // 处理这批机器动作:
        mainService.processingFinishedAction(actions);
        // 重新获取该工单:
        order = workOrderService.getWorkOrderById(orderId);
        // 此时工单的未完成数目应该等于原来要求的数目减去上面机器动作完成的数目:
        assertEquals(order.getUnfinishedAmount(), amount - productBoardAmount);
        if (semiProductBoard != null) {
            // 重新获取该半成品的存货数目:
            Inventory inventory = inventoryService.getInventory(semiProductBoard.getSpecification(), semiProductBoard.getMaterial(), semiProductBoard.getCategory().value);
            // 这个数目应该等于原有的数目加上机器动作中生成的数目:
            assertEquals(inventory.getAmount(), oldSemiProductBoardAmount + semiProductBoardAmount);
        }
    }

    @Test
    public void testCompareBoard() {
        int orderId = 3105787;
        WorkOrder order = workOrderService.getWorkOrderById(orderId);
        logger.info("order: {}", order);
        CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial(), BoardCategory.CUTTING);
        Board productBoard = new Board(order.getSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
        assertEquals(cutBoard.compareTo(productBoard), 1);
        assertEquals(productBoard.compareTo(cutBoard), -1);
        productBoard.setMaterial("热板");
        assertEquals(cutBoard.compareTo(productBoard), -1);
    }

    @Test
    public void testProcessingCutBoard() {
        int orderId = 3101334;
        WorkOrder order = workOrderService.getWorkOrderById(orderId);
        CutBoard orderCutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial(), BoardCategory.CUTTING);
        Board productBoard = BoardUtil.getStandardBoard(order.getSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
        // 暂时用工单下料板作为遗留板材:
        CutBoard legacyCutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial(), BoardCategory.CUTTING);

        machineActionService.clearAllAction();
        // 遗留板材可用时，不会生成任何语句:
        mainService.processingCutBoard(legacyCutBoard, orderCutBoard, productBoard, order.getId(), order.getSiteModule());
        assertEquals(machineActionService.getActionCount(), 0);

        machineActionService.clearAllAction();
        // 不存在遗留板材时，会有下料板取板和修边等语句，由于修边值都为零，因此只有取板语句:
        mainService.processingCutBoard(null, orderCutBoard, productBoard, order.getId(), order.getSiteModule());
        assertEquals(machineActionService.getActionCount(), 1);

        machineActionService.clearAllAction();
        legacyCutBoard.setMaterial("不存在的材质");
        // 遗留板材不可用时，会多出一句送板语句:
        mainService.processingCutBoard(legacyCutBoard, orderCutBoard, productBoard, order.getId(), order.getSiteModule());
        assertEquals(machineActionService.getActionCount(), 2);
        legacyCutBoard.setMaterial(order.getMaterial());

        machineActionService.clearAllAction();
        legacyCutBoard.setHeight(new BigDecimal(-1));
        // 遗留板材不可用时，会多出一句送板语句:
        mainService.processingCutBoard(legacyCutBoard, orderCutBoard, productBoard, order.getId(), order.getSiteModule());
        assertEquals(machineActionService.getActionCount(), 2);
    }

    @Test
    public void testProcessingNotBottomOrder() {
//        int orderId = 3098562;
        int orderId = 3118048;
        WorkOrder order = workOrderService.getWorkOrderById(orderId);
        machineActionService.clearAllAction();
        CutBoard cutBoard = mainService.processingNotBottomOrder(order, null, null);
    }
}
