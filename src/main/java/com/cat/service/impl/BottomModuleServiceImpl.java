package com.cat.service.impl;

import com.cat.enums.OrderSortPattern;
import com.cat.enums.SignalCategory;
import com.cat.pojo.*;
import com.cat.pojo.message.OrderMessage;
import com.cat.service.*;
import com.cat.utils.BoardUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * @author CAT
 */
@Slf4j
@Service("BOTTOM_PLATFORM")
public class BottomModuleServiceImpl implements ModuleService {
    private final OrderService orderService;
    private final SignalService signalService;
    private final ActionService actionService;
    private final ProcessBoardService processBoardService;

    public BottomModuleServiceImpl(OrderService orderService, SignalService signalService, ActionService actionService, ProcessBoardService processBoardService) {
        this.orderService = orderService;
        this.signalService = signalService;
        this.actionService = actionService;
        this.processBoardService = processBoardService;
    }

    @Override
    public void processOrderList(OperatingParameter param) {
        List<WorkOrder> orders = this.orderService.getBottomOrders(OrderSortPattern.get(param.getSortPattern()), param.getOrderDate());
        log.info("轿底平台模块工单数量: {}", orders.size());
        Deque<WorkOrder> orderDeque = new LinkedList<>(orders);
        this.signalService.sendTakeBoardSignal(orderDeque.peekFirst());
        while (!orderDeque.isEmpty()) {
            WorkOrder order = orderDeque.pollFirst();
            log.info("当前工单: {}", order);
            this.signalService.waitingForSignal(SignalCategory.CUTTING, this.signalService::isReceivedNewCuttingSignal);
            CuttingSignal signal = this.signalService.getNewProcessedCuttingSignal();
            MainService.RUNNING_ORDER.set(OrderMessage.of(order, signal));
            log.info("下料信号: {}", signal);
            this.processOrder(order, param, signal);
            this.actionService.processAction(orderDeque, order);
            this.signalService.waitingForSignal(SignalCategory.ROTATE, this.actionService::isAllRotateActionsCompleted);
            this.signalService.sendTakeBoardSignal(orderDeque.peekFirst());
            this.signalService.waitingForSignal(SignalCategory.ACTION, this.actionService::isAllMachineActionsProcessed);
            this.actionService.transferAllActions();
            if (this.signalService.checkStopSignal()) {
                log.info("收到流程停止信号...");
                return;
            }
        }
    }

    /**
     * 轿底流程。
     *
     * @param order         轿底工单
     * @param parameter     运行参数
     * @param cuttingSignal 下料信号
     */
    public void processOrder(WorkOrder order, OperatingParameter parameter, CuttingSignal cuttingSignal) {
        CutBoard cutBoard = BoardUtil.getCutBoard(cuttingSignal.getCuttingSize(), order.getMaterial(), cuttingSignal.getForwardEdge(), order.getId());
        log.info("下料板信息: {}", cutBoard);
        NormalBoard productBoard = BoardUtil.getStandardProduct(order.getProductSpecification(), order.getMaterial(), cutBoard.getWidth(), order.getIncompleteQuantity(), order.getId());
        log.info("成品板信息: {}", productBoard);
        NormalBoard semiProductBoard = BoardUtil.getSemiProduct(cutBoard, parameter.getFixedWidth(), productBoard);
        log.info("半成品信息: {}", semiProductBoard);
        BoardList boardList = new BoardList();
        boardList.addBoard(semiProductBoard);
        boardList.addBoard(productBoard);
        this.processBoardService.cutting(cutBoard, boardList, parameter.getWasteThreshold());
    }
}
