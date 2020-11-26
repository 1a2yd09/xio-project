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
import com.cat.enums.OrderState;
import com.cat.utils.OrderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author CAT
 */
@Component
public class MainService {
    /**
     * 同步锁对象
     */
    private static final Object LOCK = new Object();
    /**
     * 同步等待时间
     */
    private static final long WAIT_TIME = 3_000L;

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
     * 主流程
     *
     * @throws InterruptedException 等待过程被中断
     */
    public void start() throws InterruptedException {
        this.receiveStartSignal();

        OperatingParameter op = this.parameterService.getLatestOperatingParameter();
        List<StockSpecification> specs = this.stockSpecService.getGroupStockSpecs();
        // 轿底工单
        List<WorkOrder> orders = this.orderService.getBottomOrders(op.getBottomOrderSort(), op.getWorkOrderDate());
        for (WorkOrder order : orders) {
            this.orderService.updateOrderState(order, OrderState.ALREADY_STARTED);
            while (order.getUnfinishedAmount() != 0) {
                this.signalService.insertTakeBoardSignal(order.getId());
                CuttingSignal cs = this.receiveCuttingSignal(order);
                order.setCuttingSize(cs.getCuttingSize());
                this.processingBottomOrder(order, op, cs.getTowardEdge());
                this.waitForAllMachineActionsCompleted();
                this.processCompletedAction(order, BoardCategory.SEMI_PRODUCT);
            }
        }
        // 对重直梁工单
        orders = orderService.getPreprocessNotBottomOrders(op.getWorkOrderDate());
        for (int i = 0; i < orders.size(); i++) {
            WorkOrder currOrder = orders.get(i);
            WorkOrder nextOrder = OrderUtils.getFakeOrder();
            if (i < orders.size() - 1) {
                nextOrder = orders.get(i + 1);
            }
            this.orderService.updateOrderState(currOrder, OrderState.ALREADY_STARTED);
            while (currOrder.getUnfinishedAmount() != 0) {
                this.signalService.insertTakeBoardSignal(currOrder.getId());
                CuttingSignal cs = this.receiveCuttingSignal(currOrder);
                currOrder.setCuttingSize(cs.getCuttingSize());
                this.processingNotBottomOrder(currOrder, nextOrder, op, specs, cs.getTowardEdge());
                this.waitForAllMachineActionsCompleted();
                this.processCompletedAction(currOrder, BoardCategory.STOCK);
            }
        }
    }

    /**
     * 接收开工信号
     *
     * @throws InterruptedException 等待过程被中断
     */
    public void receiveStartSignal() throws InterruptedException {
        // test:
        this.signalService.insertStartSignal();
        synchronized (LOCK) {
            while (!this.signalService.isReceivedNewStartSignal()) {
                LOCK.wait(WAIT_TIME);
            }
        }
    }

    /**
     * 接收下料信号
     *
     * @param order 工单
     * @return 下料信号
     * @throws InterruptedException 等待过程被中断
     */
    public CuttingSignal receiveCuttingSignal(WorkOrder order) throws InterruptedException {
        // test:
        this.signalService.insertCuttingSignal(order.getCuttingSize(), false, order.getId());
        synchronized (LOCK) {
            while (true) {
                CuttingSignal cuttingSignal = this.signalService.getLatestNotProcessedCuttingSignal();
                if (cuttingSignal != null) {
                    return cuttingSignal;
                }
                LOCK.wait(WAIT_TIME);
            }
        }
    }

    /**
     * 等待所有机器动作都被处理完毕
     *
     * @throws InterruptedException 等待过程被中断
     */
    public void waitForAllMachineActionsCompleted() throws InterruptedException {
        // test:
        this.actionService.completedAllMachineActions();
        synchronized (LOCK) {
            while (!this.actionService.isAllMachineActionsCompleted()) {
                LOCK.wait(WAIT_TIME);
            }
        }
    }

    /**
     * 轿底流程
     *
     * @param order              轿底工单
     * @param parameter          运行参数
     * @param cutBoardLongToward 下料板朝向
     */
    public void processingBottomOrder(WorkOrder order, OperatingParameter parameter, Boolean cutBoardLongToward) {
        String material = order.getMaterial();
        int orderId = order.getId();
        BigDecimal fixedWidth = parameter.getFixedWidth();
        BigDecimal wasteThreshold = parameter.getWasteThreshold();

        CutBoard cutBoard = this.boardService.getCutBoard(order.getCuttingSize(), material, cutBoardLongToward);
        NormalBoard productBoard = this.boardService.getStandardProduct(order.getSpecification(), material, cutBoard.getWidth(), order.getUnfinishedAmount());
        NormalBoard semiProductBoard = this.boardService.getSemiProduct(cutBoard, fixedWidth, productBoard);

        if (semiProductBoard.getCutTimes() > 0) {
            this.boardService.twoStep(cutBoard, semiProductBoard, wasteThreshold, orderId);
        }
        this.boardService.threeStep(cutBoard, productBoard, wasteThreshold, orderId);
    }

    /**
     * 对重直梁流程
     *
     * @param order              对重直梁工单
     * @param nextOrder          后续对重直梁工单
     * @param parameter          运行参数
     * @param specs              库存件规格集合
     * @param cutBoardLongToward 下料板朝向
     */
    public void processingNotBottomOrder(WorkOrder order, WorkOrder nextOrder, OperatingParameter parameter, List<StockSpecification> specs, Boolean cutBoardLongToward) {
        String material = order.getMaterial();
        int orderId = order.getId();
        BigDecimal wasteThreshold = parameter.getWasteThreshold();

        CutBoard cutBoard = this.boardService.getCutBoard(order.getCuttingSize(), material, cutBoardLongToward);
        NormalBoard productBoard = this.boardService.getStandardProduct(order.getSpecification(), material, cutBoard.getWidth(), order.getUnfinishedAmount());

        if (productBoard.getCutTimes() == order.getUnfinishedAmount()) {
            NormalBoard nextProduct = this.boardService.getNextProduct(nextOrder, cutBoard, productBoard);

            if (nextProduct.getCutTimes() > 0) {
                this.boardService.twoStep(cutBoard, productBoard, wasteThreshold, orderId);
                this.boardService.threeStep(cutBoard, nextProduct, wasteThreshold, nextOrder.getId());
            } else {
                NormalBoard stockBoard = this.boardService.getMatchStock(specs, cutBoard, productBoard);

                if (stockBoard.getCutTimes() > 0) {
                    if (productBoard.getLength().compareTo(stockBoard.getLength()) >= 0) {
                        this.boardService.twoStep(cutBoard, productBoard, wasteThreshold, orderId);
                        this.boardService.threeStep(cutBoard, stockBoard, wasteThreshold, orderId);
                    } else {
                        this.boardService.twoStep(cutBoard, stockBoard, wasteThreshold, orderId);
                        this.boardService.threeStep(cutBoard, productBoard, wasteThreshold, orderId);
                    }
                } else {
                    this.boardService.threeStep(cutBoard, productBoard, wasteThreshold, orderId);
                }
            }
        } else {
            this.boardService.threeStep(cutBoard, productBoard, wasteThreshold, orderId);
        }
    }

    /**
     * 处理一组被机器处理完毕的动作
     *
     * @param order             工单
     * @param inventoryCategory 存货类型
     */
    public void processCompletedAction(WorkOrder order, BoardCategory inventoryCategory) {
        int productCount = 0;
        Inventory inventory = null;
        int inventoryCount = 0;

        for (MachineAction action : this.actionService.getAllMachineActions()) {
            // 只处理动作状态为已完成的动作
            if (ActionState.FINISHED.value.equals(action.getState())) {
                String boardCategory = action.getBoardCategory();
                if (BoardCategory.PRODUCT.value.equals(boardCategory)) {
                    productCount++;
                } else if (inventoryCategory.value.equals(boardCategory)) {
                    if (inventory == null) {
                        inventory = new Inventory(action.getBoardSpecification(), action.getBoardMaterial(), inventoryCategory.value);
                    }
                    inventoryCount++;
                }
            }
        }

        this.orderService.addOrderCompletedAmount(order, productCount);
        if (inventory != null) {
            inventory.setAmount(inventoryCount);
            this.inventoryService.updateInventoryAmount(inventory);
        }

        this.actionService.transferAllMachineActions();
        this.actionService.truncateMachineAction();
    }
}
