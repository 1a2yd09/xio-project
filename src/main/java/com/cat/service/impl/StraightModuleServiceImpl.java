package com.cat.service.impl;

import com.cat.enums.OrderSortPattern;
import com.cat.pojo.*;
import com.cat.service.*;
import com.cat.utils.BoardUtil;
import com.cat.utils.OrderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * @author CAT
 */
@Slf4j
@Service("STRAIGHT_WEIGHT")
public class StraightModuleServiceImpl extends AbstractModuleService {
    private final StockSpecService stockSpecService;

    public StraightModuleServiceImpl(SignalService signalService, ActionService actionService, OrderService orderService, ParameterService parameterService, ProcessBoardService processBoardService, StockSpecService stockSpecService) {
        super(signalService, actionService, orderService, parameterService, processBoardService);
        this.stockSpecService = stockSpecService;
    }

    @Override
    public void getOrderDeque(Deque<WorkOrder> orderDeque, OrderSortPattern sortPattern, LocalDate date) {
        Set<Integer> orderIdSet = new HashSet<>();
        for (WorkOrder order : orderDeque) {
            orderIdSet.add(order.getId());
        }
        for (WorkOrder order : this.getOrderService().getStraightOrders(sortPattern, date)) {
            if (!orderIdSet.contains(order.getId())) {
                orderDeque.offerLast(order);
            }
        }
    }

    @Override
    public void processOrder(OperatingParameter parameter, CuttingSignal cuttingSignal, WorkOrder... orders) {
        BoardUtil.changeCuttingSize(cuttingSignal);
        WorkOrder order = orders[0];
        CutBoard cutBoard = BoardUtil.getCutBoard(cuttingSignal.getCuttingSize(), order.getMaterial(), cuttingSignal.getForwardEdge(), order.getId());
        log.info("下料板信息: {}", cutBoard);
        NormalBoard productBoard = BoardUtil.getStandardProduct(order.getProductSpecification(), order.getMaterial(), cutBoard.getWidth(), order.getIncompleteQuantity(), order.getId());
        log.info("成品板信息: {}", productBoard);
        BoardList boardList = new BoardList();
        if (productBoard.getCutTimes() == order.getIncompleteQuantity()) {
            WorkOrder nextOrder = orders[1];
            NormalBoard nextProduct = BoardUtil.getNextProduct(order, nextOrder, cutBoard, productBoard);
            log.info("后续成品板信息: {}", nextProduct);
            if (nextProduct.getCutTimes() > 0) {
                boardList.addBoard(productBoard);
                boardList.addBoard(nextProduct);
            } else {
                List<StockSpecification> specs = this.stockSpecService.getGroupStockSpecs();
                log.info("库存件规格集合: {}", specs);
                NormalBoard stockBoard = BoardUtil.getMatchStock(specs, cutBoard, productBoard);
                log.info("可用的库存件信息: {}", stockBoard);
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
        this.getProcessBoardService().cutting(cutBoard, boardList, parameter.getWasteThreshold(), cuttingSignal);
    }

    @Override
    protected Integer processOrder(OperatingParameter parameter, CuttingSignal cuttingSignal, List<WorkOrder> orderList) {
        System.out.println("==========");
        orderList.forEach(System.out::println);
        WorkOrder firstOrder = orderList.get(0);
        List<NormalBoard> boardList = this.getOrderService().getBoardList(BoardUtil.changeCuttingSize(cuttingSignal), orderList, parameter.getWasteThreshold());
        System.out.println(boardList.size());
        boardList.forEach(System.out::println);
        Map<Integer, Integer> countMap = OrderUtil.calOrderProduct(boardList);
        Integer nextOrderId = OrderUtil.getNextOrderId(countMap, orderList);
        CutBoard cutBoard = BoardUtil.getCutBoard(cuttingSignal.getCuttingSize(), firstOrder.getMaterial(), cuttingSignal.getForwardEdge(), firstOrder.getId());
        this.getProcessBoardService().frontToBackCutting(cutBoard, boardList, parameter.getWasteThreshold(), cuttingSignal);
        // 等动作生成后才能知道此时正在进行的工单:
        this.getOrderService().insertRealTimeOrder(orderList);
        return nextOrderId;
    }

    @Override
    public WorkOrder[] getProcessOrders(Deque<WorkOrder> orderDeque) {
        WorkOrder currentOrder = orderDeque.isEmpty() ? OrderUtil.getFakeOrder() : orderDeque.pollFirst();
        WorkOrder nextOrder = orderDeque.isEmpty() ? OrderUtil.getFakeOrder() : orderDeque.pollFirst();
        return new WorkOrder[]{currentOrder, nextOrder};
    }
}
