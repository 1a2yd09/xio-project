package com.cat.service.impl;

import com.cat.enums.OrderSortPattern;
import com.cat.pojo.*;
import com.cat.service.*;
import com.cat.utils.BoardUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        for (WorkOrder order : orders) {
            log.info("当前工单: {}", order);
            while (order.getIncompleteQuantity() != 0) {
                this.signalService.insertTakeBoardSignal(order.getId());
                CuttingSignal cuttingSignal = this.signalService.receiveNewCuttingSignal(order);
                log.info("下料信号: {}", cuttingSignal);
                this.processBottomOrder(order, param, cuttingSignal);
                this.actionService.processCompletedAction(order);
                if (this.signalService.checkingForNewProcessStopSignal()) {
                    log.info("收到流程停止信号...");
                    return;
                }
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
    public void processBottomOrder(WorkOrder order, OperatingParameter parameter, CuttingSignal cuttingSignal) {
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
