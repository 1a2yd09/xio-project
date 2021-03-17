package com.cat.service;

import com.cat.enums.ActionState;
import com.cat.enums.BoardCategory;
import com.cat.enums.OrderModule;
import com.cat.pojo.*;
import com.cat.utils.BoardUtil;
import com.cat.utils.OrderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CAT
 */
@Slf4j
@Service
public class MainService {
    @Autowired
    ProcessBoardService processBoardService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    SignalService signalService;
    @Autowired
    OrderService orderService;
    @Autowired
    ActionService actionService;
    @Autowired
    InventoryService inventoryService;
    @Autowired
    StockSpecService stockSpecService;

    /**
     * 主流程。
     *
     * @param orderModule 工单模块
     * @throws InterruptedException 等待过程被中断
     */
    public void start(OrderModule orderModule) throws InterruptedException {
        this.signalService.waitingForNewProcessStartSignal();

        OperatingParameter param = this.parameterService.getLatestOperatingParameter();
        log.info("流程运行参数: {}", param);
        List<StockSpecification> specs = this.stockSpecService.getGroupStockSpecs();
        log.info("流程库存件规格列表: {}", specs);
        List<WorkOrder> orders = this.orderService.getProductionOrders(orderModule, param);
        log.info("{}模块工单数量为: {}", orderModule, orders.size());

        for (int i = 0; i < orders.size(); i++) {
            WorkOrder currentOrder = orders.get(i);
            log.info("当前工单信息: {}", currentOrder);
            while (currentOrder.getIncompleteQuantity() != 0) {
                this.signalService.insertTakeBoardSignal(currentOrder.getId());
                CuttingSignal cuttingSignal = this.signalService.receiveNewCuttingSignal(currentOrder);
                log.info("下料信号内容: {}", cuttingSignal);

                if (OrderModule.BOTTOM_PLATFORM == orderModule) {
                    this.processingBottomOrder(currentOrder, param, cuttingSignal);
                } else {
                    WorkOrder nextOrder = OrderUtil.getFakeOrder();
                    if (i < orders.size() - 1) {
                        nextOrder = orders.get(i + 1);
                    }
                    log.info("后续工单信息: {}", nextOrder);
                    this.processingNotBottomOrder(currentOrder, nextOrder, param, specs, cuttingSignal);
                }

                List<MachineAction> actions = this.actionService.getAllMachineActions();
                log.info("机器动作列表:");
                for (MachineAction action : actions) {
                    log.info("{}", action);
                }
                this.processCompletedAction();
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
    public void processingBottomOrder(WorkOrder order, OperatingParameter parameter, CuttingSignal cuttingSignal) {
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

    /**
     * 对重直梁流程。
     *
     * @param order         对重直梁工单
     * @param nextOrder     后续对重直梁工单
     * @param parameter     运行参数
     * @param specs         库存件规格集合
     * @param cuttingSignal 下料信号
     */
    public void processingNotBottomOrder(WorkOrder order, WorkOrder nextOrder, OperatingParameter parameter, List<StockSpecification> specs, CuttingSignal cuttingSignal) {
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

    /**
     * 处理一组被机器处理完毕的动作。
     */
    public void processCompletedAction() throws InterruptedException {
        this.actionService.waitingForAllMachineActionsCompleted();

        Map<Integer, Integer> map = new HashMap<>(2);
        Inventory inventory = null;
        int inventoryCount = 0;

        for (MachineAction action : this.actionService.getAllMachineActions()) {
            // 只处理动作状态为已完成的动作:
            if (ActionState.COMPLETED.value.equals(action.getState())) {
                BoardCategory bc = BoardCategory.get(action.getBoardCategory());
                switch (bc) {
                    case PRODUCT:
                        map.put(action.getOrderId(), map.getOrDefault(action.getOrderId(), 0) + 1);
                        break;
                    case STOCK:
                    case SEMI_PRODUCT:
                        if (inventory == null) {
                            inventory = new Inventory(action.getBoardSpecification(), action.getBoardMaterial(), bc.value);
                        }
                        inventoryCount++;
                        break;
                    default:
                        break;
                }
            }
        }

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            this.orderService.addOrderCompletedQuantity(this.orderService.getOrderById(entry.getKey()), entry.getValue());
        }
        if (inventory != null) {
            inventory.setQuantity(inventoryCount);
            this.inventoryService.updateInventoryQuantity(inventory);
        }

        this.actionService.transferAllActions();
    }
}
