package com.cat.service;

import com.cat.enums.ActionState;
import com.cat.enums.BoardCategory;
import com.cat.enums.OrderModule;
import com.cat.enums.OrderSortPattern;
import com.cat.pojo.*;
import com.cat.pojo.message.OrderMessage;
import com.cat.utils.BoardUtil;
import com.cat.utils.OrderUtil;
import com.cat.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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
    @Autowired
    MailService mailService;

    private static final AtomicReference<OrderMessage> CURRENT_ORDER = new AtomicReference<>(null);

    /**
     * 主流程。
     */
    public void start() {
        ThreadUtil.WORK_THREAD_RUNNING.set(true);
        try {
            // while 循环:
            this.signalService.waitingForNewProcessStartSignal();
            OperatingParameter param = this.parameterService.getLatestOperatingParameter();
            OrderModule orderModule = OrderModule.get(param.getOrderModule());
            switch (orderModule) {
                case BOTTOM_PLATFORM:
                    this.bottom(param);
                    break;
                case STRAIGHT_WEIGHT:
                    this.notBottom(param);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            // 写异常日志到文件
            log.error(e.getMessage(), e);
            // 发异常邮件
            this.mailService.sendWorkErrorMail(CURRENT_ORDER.get());
            // 标记异常工单
            ThreadUtil.WORK_THREAD_RUNNING.set(false);
        }
    }

    private void bottom(OperatingParameter param) {
        List<WorkOrder> orders = this.orderService.getBottomOrders(OrderSortPattern.get(param.getSortPattern()), param.getOrderDate());
        log.info("轿底平台模块工单数量: {}", orders.size());
        for (WorkOrder order : orders) {
            log.info("当前工单: {}", order);
            while (order.getIncompleteQuantity() != 0) {
                this.signalService.insertTakeBoardSignal(order.getId());
                CuttingSignal cuttingSignal = this.signalService.receiveNewCuttingSignal(order);
                CURRENT_ORDER.set(OrderMessage.of(order, cuttingSignal));
                log.info("下料信号: {}", cuttingSignal);
                this.processingBottomOrder(order, param, cuttingSignal);
                List<MachineAction> actions = this.actionService.getAllMachineActions();
                log.info("机器动作列表:");
                for (MachineAction action : actions) {
                    log.info("{}", action);
                }
                this.processCompletedAction(order);
                if (this.signalService.checkingForNewProcessStopSignal()) {
                    log.info("收到流程停止信号...");
                    return;
                }
            }
        }
    }

    private void notBottom(OperatingParameter param) {
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
                CURRENT_ORDER.set(OrderMessage.of(currOrder, cuttingSignal));
                log.info("下料信号: {}", cuttingSignal);
                WorkOrder nextOrder = i < orders.size() - 1 ? orders.get(i + 1) : OrderUtil.getFakeOrder();
                log.info("后续工单: {}", nextOrder);
                this.processingNotBottomOrder(currOrder, nextOrder, param, specs, cuttingSignal);
                List<MachineAction> actions = this.actionService.getAllMachineActions();
                log.info("机器动作列表:");
                for (MachineAction action : actions) {
                    log.info("{}", action);
                }
                this.processCompletedAction(currOrder, nextOrder);
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
    public void processCompletedAction(WorkOrder... orders) {
        this.actionService.waitingForAllMachineActionsCompleted();

        Map<Integer, Integer> map = new HashMap<>(4);
        for (WorkOrder order : orders) {
            map.put(order.getId(), 0);
        }
        Inventory inventory = null;
        int inventoryCount = 0;

        for (MachineAction action : this.actionService.getAllMachineActions()) {
            // 只处理动作状态为已完成的动作:
            if (ActionState.COMPLETED.value.equals(action.getState())) {
                String bc = action.getBoardCategory();
                if (BoardCategory.PRODUCT.value.equals(bc)) {
                    map.put(action.getOrderId(), map.getOrDefault(action.getOrderId(), 0) + 1);
                } else if (BoardCategory.STOCK.value.equals(bc) || BoardCategory.SEMI_PRODUCT.value.equals(bc)) {
                    if (inventory == null) {
                        inventory = new Inventory(BoardUtil.getStandardSpecStr(action.getBoardSpecification()), action.getBoardMaterial(), bc);
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

        this.actionService.transferAllActions();
    }
}
