package com.cat;

import com.cat.entity.*;
import com.cat.entity.enums.BoardCategory;
import com.cat.service.*;
import com.cat.util.BoardUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppConfigTest {

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
        WorkOrder order = workOrderService.getWorkOrderById(3098528);
        CutBoard cutBoard = boardService.pickingBoard(order.getCuttingSize(), order.getMaterial(), order.getId(), order.getSiteModule());
        BigDecimal width = cutBoard.getWidth();
        System.out.println(width);
        System.out.println(width.subtract(BigDecimal.TEN));
        System.out.println(width);
    }

    @Test
    public void testProcessBottomOrder1() {
        WorkOrder order = workOrderService.getWorkOrderById(3101334);
        machineActionService.clearAllAction();
        mainService.processBottomOrder(order);
        assertEquals(machineActionService.getActionCount(), 13);
    }

    @Test
    public void testProcessBottomOrder2() {
        WorkOrder order = workOrderService.getWorkOrderById(3098967);
        machineActionService.clearAllAction();
        mainService.processBottomOrder(order);
        assertEquals(machineActionService.getActionCount(), 5);
    }

    @Test
    public void testProcessBottomOrder3() {
        WorkOrder order = workOrderService.getWorkOrderById(3101166);
        machineActionService.clearAllAction();
        mainService.processBottomOrder(order);
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
        WorkOrder order = workOrderService.getWorkOrderById(3101334);
        machineActionService.clearAllAction();
        mainService.processBottomOrder(order);
        List<MachineAction> actions = machineActionService.getAllActions();
        mainService.processFinishedAction(actions);
        order = workOrderService.getWorkOrderById(3101334);
        assertEquals(order.getUnfinishedAmount(), 0);
        assertEquals(inventoryService.getInventoryCount(), 1);
    }
}
