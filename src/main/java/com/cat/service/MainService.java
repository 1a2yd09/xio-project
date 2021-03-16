package com.cat.service;

import com.cat.enums.ActionState;
import com.cat.enums.BoardCategory;
import com.cat.enums.OrderModule;
import com.cat.pojo.*;
import com.cat.utils.OrderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CAT
 */
@Service
public class MainService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    BoardService boardService;
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
        logger.info("流程运行参数: {}", param);
        List<StockSpecification> specs = this.stockSpecService.getGroupStockSpecs();
        logger.info("流程库存件规格列表: {}", specs);
        List<WorkOrder> orders = this.orderService.getProductionOrders(orderModule, param);
        logger.info("{}模块工单数量为: {}", orderModule, orders.size());

        for (int i = 0; i < orders.size(); i++) {
            WorkOrder currentOrder = orders.get(i);
            logger.info("当前工单信息: {}", currentOrder);
            while (currentOrder.getIncompleteQuantity() != 0) {
                this.signalService.insertTakeBoardSignal(currentOrder.getId());
                CuttingSignal cuttingSignal = this.signalService.receiveNewCuttingSignal(currentOrder);
                logger.info("下料信号内容: {}", cuttingSignal);

                if (OrderModule.BOTTOM_PLATFORM == orderModule) {
                    this.processingBottomOrder(currentOrder, param, cuttingSignal);
                    List<MachineAction> actions = this.actionService.getAllMachineActions();
                    logger.info("机器动作列表:");
                    for (MachineAction action : actions) {
                        logger.info("{}", action);
                    }
                    this.processCompletedAction(BoardCategory.SEMI_PRODUCT, currentOrder);
                } else {
                    WorkOrder nextOrder = OrderUtil.getFakeOrder();
                    if (i < orders.size() - 1) {
                        nextOrder = orders.get(i + 1);
                    }
                    logger.info("后续工单信息: {}", nextOrder);
                    this.processingNotBottomOrder(currentOrder, nextOrder, param, specs, cuttingSignal);
                    List<MachineAction> actions = this.actionService.getAllMachineActions();
                    logger.info("机器动作列表:");
                    for (MachineAction action : actions) {
                        logger.info("{}", action);
                    }
                    this.processCompletedAction(BoardCategory.STOCK, currentOrder, nextOrder);
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
    public void processingBottomOrder(WorkOrder order, OperatingParameter parameter, CuttingSignal cuttingSignal) {
        CutBoard cutBoard = this.boardService.getCutBoard(cuttingSignal.getCuttingSize(), order.getMaterial(), cuttingSignal.getForwardEdge(), order.getId());
        logger.info("下料板信息: {}", cutBoard);
        NormalBoard productBoard = this.boardService.getStandardProduct(order.getProductSpecification(), order.getMaterial(), cutBoard.getWidth(), order.getIncompleteQuantity(), order.getId());
        logger.info("成品板信息: {}", productBoard);
        NormalBoard semiProductBoard = this.boardService.getSemiProduct(cutBoard, parameter.getFixedWidth(), productBoard);
        logger.info("半成品信息: {}", semiProductBoard);

        BoardList boardList = new BoardList();
        boardList.addBoard(semiProductBoard);
        boardList.addBoard(productBoard);

        this.boardService.newCutting(cutBoard, boardList, parameter.getWasteThreshold(), order.getId());
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
        Integer currOrderId = order.getId();
        CutBoard cutBoard = this.boardService.getCutBoard(cuttingSignal.getCuttingSize(), order.getMaterial(), cuttingSignal.getForwardEdge(), order.getId());
        logger.info("下料板信息: {}", cutBoard);
        NormalBoard productBoard = this.boardService.getStandardProduct(order.getProductSpecification(), order.getMaterial(), cutBoard.getWidth(), order.getIncompleteQuantity(), order.getId());
        logger.info("成品板信息: {}", productBoard);

        BoardList boardList = new BoardList();

        if (productBoard.getCutTimes() == order.getIncompleteQuantity()) {
            NormalBoard nextProduct = this.boardService.getNextProduct(nextOrder, cutBoard, productBoard);
            logger.info("后续成品板信息: {}", nextProduct);
            if (nextProduct.getCutTimes() > 0) {
                boardList.addBoard(productBoard);
                boardList.addBoard(nextProduct);
            } else {
                NormalBoard stockBoard = this.boardService.getMatchStock(specs, cutBoard, productBoard);
                logger.info("库存件信息: {}", stockBoard);
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

        this.boardService.newCutting(cutBoard, boardList, parameter.getWasteThreshold(), currOrderId);
    }

    /**
     * 处理一组被机器处理完毕的动作。
     *
     * @param inventoryCategory 存货类型
     * @param orders            工单列表
     */
    public void processCompletedAction(BoardCategory inventoryCategory, WorkOrder... orders) throws InterruptedException {
        this.actionService.waitingForAllMachineActionsCompleted();

        Map<Integer, Integer> map = new HashMap<>(orders.length);
        for (WorkOrder order : orders) {
            map.put(order.getId(), 0);
        }
        Inventory inventory = null;
        int inventoryCount = 0;

        for (MachineAction action : this.actionService.getAllMachineActions()) {
            // 只处理动作状态为已完成的动作:
            if (ActionState.COMPLETED.value.equals(action.getState())) {
                String boardCategory = action.getBoardCategory();
                if (BoardCategory.PRODUCT.value.equals(boardCategory)) {
                    Integer orderId = action.getOrderId();
                    map.put(orderId, map.get(orderId) + 1);
                } else if (inventoryCategory.value.equals(boardCategory)) {
                    if (inventory == null) {
                        inventory = new Inventory(action.getBoardSpecification(), action.getBoardMaterial(), inventoryCategory.value);
                    }
                    inventoryCount++;
                }
            }
        }

        for (WorkOrder order : orders) {
            this.orderService.addOrderCompletedQuantity(order, map.get(order.getId()));
        }
        if (inventory != null) {
            inventory.setQuantity(inventoryCount);
            this.inventoryService.updateInventoryQuantity(inventory);
        }

        this.actionService.transferAllMachineActions();
        this.actionService.truncateMachineAction();
    }
}
