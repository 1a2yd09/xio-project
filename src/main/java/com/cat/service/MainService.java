package com.cat.service;

import com.cat.entity.bean.Inventory;
import com.cat.entity.bean.MachineAction;
import com.cat.entity.bean.WorkOrder;
import com.cat.entity.board.CutBoard;
import com.cat.entity.board.NormalBoard;
import com.cat.entity.param.OperatingParameter;
import com.cat.entity.param.StockSpecification;
import com.cat.entity.signal.CuttingSignal;
import com.cat.enums.ActionState;
import com.cat.enums.BoardCategory;
import com.cat.enums.ControlSignalCategory;
import com.cat.enums.OrderModule;
import com.cat.utils.OrderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CAT
 */
@Component
public class MainService {
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
        List<StockSpecification> specs = this.stockSpecService.getGroupStockSpecs();
        List<WorkOrder> orders = this.orderService.getProductionOrders(orderModule, param);

        for (int i = 0; i < orders.size(); i++) {
            WorkOrder currentOrder = orders.get(i);
            while (currentOrder.getIncompleteQuantity() != 0) {
                this.signalService.insertTakeBoardSignal(currentOrder.getId());
                CuttingSignal cuttingSignal = this.signalService.receiveNewCuttingSignal(currentOrder);

                if (OrderModule.BOTTOM_PLATFORM == orderModule) {
                    this.processingBottomOrder(currentOrder, param, cuttingSignal);
                    this.processCompletedAction(BoardCategory.SEMI_PRODUCT, currentOrder);
                } else {
                    WorkOrder nextOrder = OrderUtils.getFakeOrder();
                    if (i < orders.size() - 1) {
                        nextOrder = orders.get(i + 1);
                    }
                    this.processingNotBottomOrder(currentOrder, nextOrder, param, specs, cuttingSignal);
                    this.processCompletedAction(BoardCategory.STOCK, currentOrder, nextOrder);
                }

                if (signalService.isReceivedNewProcessControlSignal(ControlSignalCategory.STOP)) {
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
        CutBoard cutBoard = this.boardService.getCutBoard(cuttingSignal.getCuttingSize(), order.getMaterial(), cuttingSignal.getForwardEdge());
        NormalBoard productBoard = this.boardService.getStandardProduct(order.getProductSpecification(), order.getMaterial(), cutBoard.getWidth(), order.getIncompleteQuantity());
        NormalBoard semiProductBoard = this.boardService.getSemiProduct(cutBoard, parameter.getFixedWidth(), productBoard);

        List<Map<Integer, NormalBoard>> normalBoards = new ArrayList<>();
        normalBoards.add(Map.of(order.getId(), semiProductBoard));
        normalBoards.add(Map.of(order.getId(), productBoard));

        this.boardService.cutting(cutBoard, normalBoards, parameter.getWasteThreshold(), order.getId());
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
        CutBoard cutBoard = this.boardService.getCutBoard(cuttingSignal.getCuttingSize(), order.getMaterial(), cuttingSignal.getForwardEdge());
        NormalBoard productBoard = this.boardService.getStandardProduct(order.getProductSpecification(), order.getMaterial(), cutBoard.getWidth(), order.getIncompleteQuantity());

        List<Map<Integer, NormalBoard>> normalBoards = new ArrayList<>();

        if (productBoard.getCutTimes() == order.getIncompleteQuantity()) {
            NormalBoard nextProduct = this.boardService.getNextProduct(nextOrder, cutBoard, productBoard);
            if (nextProduct.getCutTimes() > 0) {
                normalBoards.add(Map.of(currOrderId, productBoard));
                normalBoards.add(Map.of(nextOrder.getId(), nextProduct));
            } else {
                NormalBoard stockBoard = this.boardService.getMatchStock(specs, cutBoard, productBoard);
                if (stockBoard.getCutTimes() > 0) {
                    if (productBoard.getLength().compareTo(stockBoard.getLength()) >= 0) {
                        normalBoards.add(Map.of(currOrderId, productBoard));
                        normalBoards.add(Map.of(currOrderId, stockBoard));
                    } else {
                        normalBoards.add(Map.of(currOrderId, stockBoard));
                        normalBoards.add(Map.of(currOrderId, productBoard));
                    }
                } else {
                    normalBoards.add(Map.of(currOrderId, productBoard));
                }
            }
        } else {
            normalBoards.add(Map.of(currOrderId, productBoard));
        }

        this.boardService.cutting(cutBoard, normalBoards, parameter.getWasteThreshold(), currOrderId);
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
