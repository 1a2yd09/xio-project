package com.cat.service.impl;

import com.cat.enums.OrderSortPattern;
import com.cat.pojo.*;
import com.cat.pojo.message.OrderMessage;
import com.cat.service.*;
import com.cat.utils.BoardUtil;
import com.cat.utils.OrderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author CAT
 */
@Slf4j
@Service("STRAIGHT_WEIGHT")
public class StraightModuleServiceImpl implements ModuleService {
    private final ProcessBoardService processBoardService;
    private final StockSpecService stockSpecService;
    private final OrderService orderService;
    private final SignalService signalService;
    private final ActionService actionService;

    public StraightModuleServiceImpl(ProcessBoardService processBoardService, StockSpecService stockSpecService, OrderService orderService, SignalService signalService, ActionService actionService) {
        this.processBoardService = processBoardService;
        this.stockSpecService = stockSpecService;
        this.orderService = orderService;
        this.signalService = signalService;
        this.actionService = actionService;
    }

    @Override
    public void processOrderList(OperatingParameter param) {
        List<StockSpecification> specs = this.stockSpecService.getGroupStockSpecs();
        log.info("库存件规格集合: {}", specs);
        List<WorkOrder> orders = this.orderService.getPreprocessNotBottomOrders(OrderSortPattern.get(param.getSortPattern()), param.getOrderDate());
        log.info("直梁对重模块工单数量: {}", orders.size());
        for (int i = 0; i < orders.size(); i++) {
            WorkOrder currOrder = orders.get(i);
            log.info("当前工单: {}", currOrder);
            while (currOrder.getIncompleteQuantity() != 0) {
                this.signalService.insertTakeBoardSignal(currOrder.getId());
                CuttingSignal cuttingSignal = this.signalService.receiveNewCuttingSignal(currOrder);
                log.info("下料信号: {}", cuttingSignal);
                MainService.RUNNING_ORDER.set(OrderMessage.of(currOrder, cuttingSignal));
                WorkOrder nextOrder = i < orders.size() - 1 ? orders.get(i + 1) : OrderUtil.getFakeOrder();
                log.info("后续工单: {}", nextOrder);
                this.processStraightOrder(currOrder, nextOrder, param, specs, cuttingSignal);
                this.actionService.processCompletedAction(currOrder, nextOrder);
                if (this.signalService.checkingForNewProcessStopSignal()) {
                    log.info("收到流程停止信号...");
                    return;
                }
            }
        }
    }

    /**
     * 对重直梁流程。
     *
     * @param order         对重直梁工单
     * @param nextOrder     后续对重直梁工单
     * @param parameter     运行参数
     * @param specs         库存件规格集合
     * @param cuttingSignal 下料信号
     */
    public void processStraightOrder(WorkOrder order, WorkOrder nextOrder, OperatingParameter parameter, List<StockSpecification> specs, CuttingSignal cuttingSignal) {
        CutBoard cutBoard = BoardUtil.getCutBoard(cuttingSignal.getCuttingSize(), order.getMaterial(), cuttingSignal.getForwardEdge(), order.getId());
        log.info("下料板信息: {}", cutBoard);
        NormalBoard productBoard = BoardUtil.getStandardProduct(order.getProductSpecification(), order.getMaterial(), cutBoard.getWidth(), order.getIncompleteQuantity(), order.getId());
        log.info("成品板信息: {}", productBoard);
        BoardList boardList = new BoardList();
        if (productBoard.getCutTimes() == order.getIncompleteQuantity()) {
            NormalBoard nextProduct = BoardUtil.getNextProduct(nextOrder, cutBoard, productBoard);
            log.info("后续成品板信息: {}", nextProduct);
            if (nextProduct.getCutTimes() > 0) {
                boardList.addBoard(productBoard);
                boardList.addBoard(nextProduct);
            } else {
                NormalBoard stockBoard = BoardUtil.getMatchStock(specs, cutBoard, productBoard);
                log.info("库存件信息: {}", stockBoard);
                if (stockBoard.getCutTimes() > 0) {
                    if (productBoard.getLength().compareTo(stockBoard.getLength()) >= 0) {
                        boardList.addBoard(productBoard);
                        boardList.addBoard(stockBoard);
                    } else {
                        boardList.addBoard(stockBoard);
                        boardList.addBoard(productBoard);
                    }
                } else {
                    boardList.addBoard(productBoard);
                }
            }
        } else {
            boardList.addBoard(productBoard);
        }
        this.processBoardService.cutting(cutBoard, boardList, parameter.getWasteThreshold());
    }
}
