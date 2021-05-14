package com.cat.service.impl;

import com.cat.enums.OrderSortPattern;
import com.cat.pojo.*;
import com.cat.service.ActionService;
import com.cat.service.OrderService;
import com.cat.service.ProcessBoardService;
import com.cat.service.SignalService;
import com.cat.utils.BoardUtil;
import com.cat.utils.OrderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Deque;

/**
 * @author CAT
 */
@Slf4j
@Service("BOTTOM_PLATFORM")
public class BottomModuleServiceImpl extends AbstractModuleService {
    public BottomModuleServiceImpl(SignalService signalService, ActionService actionService, OrderService orderService, ProcessBoardService processBoardService) {
        super(signalService, actionService, orderService, processBoardService);
    }

    @Override
    public Deque<WorkOrder> getOrderDeque(OrderSortPattern sortPattern, LocalDate date) {
        return this.getOrderService().getBottomDeque(sortPattern, date);
    }

    @Override
    public void processOrder(OperatingParameter parameter, CuttingSignal cuttingSignal, WorkOrder... orders) {
        WorkOrder order = orders[0];
        BoardUtil.changeCuttingSize(cuttingSignal);
        CutBoard cutBoard = BoardUtil.getCutBoard(cuttingSignal.getCuttingSize(), order.getMaterial(), cuttingSignal.getForwardEdge(), order.getId());
        log.info("下料板信息: {}", cutBoard);
        NormalBoard productBoard = BoardUtil.getStandardProduct(order.getProductSpecification(), order.getMaterial(), cutBoard.getWidth(), order.getIncompleteQuantity(), order.getId());
        log.info("成品板信息: {}", productBoard);
        NormalBoard semiProductBoard = BoardUtil.getSemiProduct(cutBoard, parameter.getFixedWidth(), productBoard);
        log.info("半成品信息: {}", semiProductBoard);
        BoardList boardList = new BoardList();
        boardList.addBoard(semiProductBoard);
        boardList.addBoard(productBoard);
        this.getProcessBoardService().cutting(cutBoard, boardList, parameter.getWasteThreshold(), cuttingSignal);
    }

    @Override
    public WorkOrder[] getProcessOrders(Deque<WorkOrder> orderDeque) {
        WorkOrder currentOrder = orderDeque.isEmpty() ? OrderUtil.getFakeOrder() : orderDeque.pollFirst();
        return new WorkOrder[]{currentOrder};
    }
}
