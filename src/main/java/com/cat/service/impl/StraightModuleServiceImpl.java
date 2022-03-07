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
        // cuttingSignal是指已经被放在机床上的板材，该板用来生产指定的工单要求的板材，所以需要进行筛选
        List<WorkOrder> filterOrderList = OrderUtil.filterOrderList(cuttingSignal.getOrderId(), orderList);
        System.out.println("当前头部工单: " + filterOrderList.get(0));
        // 库存件存在多种规格，板材厚度可能不同，所以需要根据当前工单板材的厚度对库存板要求的厚度进行匹配，相同的才能进行生产库存板
        filterOrderList.add(this.stockSpecService.getStockWorkOrder(filterOrderList.get(filterOrderList.size() - 1)));
        filterOrderList.forEach(System.out::println);
        WorkOrder firstOrder = filterOrderList.get(0);
        // 函数内部会将列表中的工单移除，导致取板信号出错，但不会修改工单信息:
        // getBoardList()对一块指定规格的原料板进行规划，尽可能多的生产出工单要求的板材，也有可能有库存板
        List<NormalBoard> boardList = this.getOrderService().getBoardList(BoardUtil.changeCuttingSize(cuttingSignal), new ArrayList<>(filterOrderList), parameter.getWasteThreshold());
//        System.out.println(boardList.size());
        System.out.println("==========");
        boardList.forEach(System.out::println);
        // 对规划好的板材列表进行统计
        Map<Integer, Integer> countMap = OrderUtil.calOrderProduct(boardList);
        // 移除最后一个？
        filterOrderList.remove(filterOrderList.size() - 1);
        // 如果生产的板材数量能够满足当前工单的要求，那么相当于当前工单已经完成了，取出工单列表的下一个工单
        Integer nextOrderId = OrderUtil.getNextOrderId(countMap, orderList);
        // 对原料板进行包装CutBoard类型
        CutBoard cutBoard = BoardUtil.getCutBoard(cuttingSignal.getCuttingSize(), firstOrder.getMaterial(), cuttingSignal.getForwardEdge(), firstOrder.getId());
        // 根据规划好的板材列表boardList对cutBoard进行实际机器动作生产
        this.getProcessBoardService().frontToBackCutting(cutBoard, boardList, parameter.getWasteThreshold(), cuttingSignal);
        // 等动作生成后才能知道此时正在进行的工单:
        this.getOrderService().insertRealTimeOrder(filterOrderList);
        return nextOrderId;
    }

    @Override
    public WorkOrder[] getProcessOrders(Deque<WorkOrder> orderDeque) {
        WorkOrder currentOrder = orderDeque.isEmpty() ? OrderUtil.getFakeOrder() : orderDeque.pollFirst();
        WorkOrder nextOrder = orderDeque.isEmpty() ? OrderUtil.getFakeOrder() : orderDeque.pollFirst();
        return new WorkOrder[]{currentOrder, nextOrder};
    }
}
