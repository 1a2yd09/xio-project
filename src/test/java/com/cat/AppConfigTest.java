package com.cat;

import com.cat.entity.*;
import com.cat.entity.enums.BoardCategory;
import com.cat.entity.enums.SignalCategory;
import com.cat.service.*;
import com.cat.util.BoardUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AppConfigTest {

    static ApplicationContext context;
    static SignalService signalService;
    static WorkOrderService workOrderService;
    static ParameterService parameterService;
    static MachineActionService machineActionService;
    static BoardService boardService;
    static MainService mainService;

    @BeforeAll
    public static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        signalService = context.getBean(SignalService.class);
        workOrderService = context.getBean(WorkOrderService.class);
        parameterService = context.getBean(ParameterService.class);
        machineActionService = context.getBean(MachineActionService.class);
        boardService = context.getBean(BoardService.class);
        mainService = context.getBean(MainService.class);
    }

    @Test
    public void testGetSignal() {
        Signal signal = signalService.getLatestSignal(SignalCategory.START_WORK);
        assertNotNull(signal);
        System.out.println(signal);
    }

    @Test
    public void testGetLatestSignal() {
        boolean flag = signalService.isReceivedNewSignal(SignalCategory.START_WORK);
        assertTrue(flag);
    }

    @Test
    public void getBottomOrders() {
        OperatingParameter op = parameterService.getLatestOperatingParameter();
        List<WorkOrder> workOrders = workOrderService.getBottomOrders(op.getBottomOrderSort(), op.getWorkOrderDate());
//        List<WorkOrder> workOrders = workOrderService.getBottomOrders(op.getWorkOrderDate());
        assertEquals(workOrders.size(), 914);
        workOrders.forEach(System.out::println);
    }

    @Test
    public void testGetParameter() {
//        OperatingParameter p = parameterService.getLatestOperatingParameter();
        TrimmingParameter p = parameterService.getLatestTrimmingParameter();
        assertNotNull(p);
        System.out.println(p);
    }

    @Test
    public void testGetCutBoard() {
        WorkOrder order = workOrderService.getWorkOrderById(3098925);
        CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial(), BoardCategory.CUTTING, 0);
        assertNotNull(cutBoard);
        System.out.println(cutBoard);
    }

    @Test
    public void testAddPickAction() {
        WorkOrder order = workOrderService.getWorkOrderById(3098925);
        System.out.println(order);
        CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial(), BoardCategory.CUTTING, 0);
        System.out.println(cutBoard);
        machineActionService.clearAllAction();
        machineActionService.addPickAction(cutBoard, order.getId(), order.getSiteModule());
    }

    @Test
    public void testAddRotateAction() {
        WorkOrder order = workOrderService.getWorkOrderById(3098925);
        System.out.println(order);
        CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial(), BoardCategory.CUTTING, 0);
        System.out.println(cutBoard);
        machineActionService.clearAllAction();
        machineActionService.addRotateAction(cutBoard, order.getId(), order.getSiteModule());
    }

    @Test
    public void testAddCutAction() {
        WorkOrder order = workOrderService.getWorkOrderById(3098925);
        System.out.println(order);
        CutBoard cutBoard = new CutBoard(order.getCuttingSize(), order.getMaterial(), BoardCategory.CUTTING, 0);
        System.out.println(cutBoard);
        machineActionService.clearAllAction();
        machineActionService.addCuttingAction(cutBoard, order.getId(), order.getSiteModule());
    }

    @Test
    public void testProcessBottomOrder() {
//        WorkOrder order = workOrderService.getWorkOrderById(3101334);
//        WorkOrder order = workOrderService.getWorkOrderById(3098967);
        WorkOrder order = workOrderService.getWorkOrderById(3101166);
        machineActionService.clearAllAction();
        mainService.processBottomOrder(order);
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

    @Test
    public void testPickingBoard() {
        WorkOrder order = workOrderService.getWorkOrderById(3098528);
        System.out.println(order);
        CutBoard cutBoard = boardService.pickingBoard(order.getCuttingSize(), order.getMaterial(), order.getId(), order.getSiteModule());
        System.out.println(cutBoard);
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
}
