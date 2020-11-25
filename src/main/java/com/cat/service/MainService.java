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
    private static final Object LOCK = new Object();
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

    public void startService() throws InterruptedException {
        // test:
        this.signalService.insertStartSignal();
        synchronized (LOCK) {
            while (!this.signalService.isReceivedNewStartSignal()) {
                LOCK.wait(WAIT_TIME);
            }
        }

        OperatingParameter op = this.parameterService.getLatestOperatingParameter();
        List<StockSpecification> specs = this.stockSpecService.getGroupStockSpecs();

        List<WorkOrder> orders = this.orderService.getBottomOrders(op.getBottomOrderSort(), op.getWorkOrderDate());

        for (WorkOrder order : orders) {
            this.orderService.updateOrderState(order, OrderState.ALREADY_STARTED);
            while (order.getUnfinishedAmount() != 0) {
                this.signalService.insertTakeBoardSignal(order.getId());
                CuttingSignal cs = this.receiveCuttingSignal(order);
                order.setCuttingSize(cs.getCuttingSize());
                this.processingBottomOrder(order, op, cs.getTowardEdge());
                // test:
                this.actionService.completedAllMachineActions();
                synchronized (LOCK) {
                    while (!this.actionService.isAllMachineActionsCompleted()) {
                        LOCK.wait(WAIT_TIME);
                    }
                }
                this.processCompletedAction(order, BoardCategory.SEMI_PRODUCT);
            }
        }

        orders = orderService.getPreprocessNotBottomOrders(op.getWorkOrderDate());

        for (int i = 0; i < orders.size(); i++) {
            WorkOrder order = orders.get(i);
            WorkOrder nextOrder = OrderUtils.getFakeOrder();
            if (i < orders.size() - 1) {
                nextOrder = orders.get(i + 1);
            }
            this.orderService.updateOrderState(order, OrderState.ALREADY_STARTED);
            while (order.getUnfinishedAmount() != 0) {
                this.signalService.insertTakeBoardSignal(order.getId());
                CuttingSignal cs = this.receiveCuttingSignal(order);
                order.setCuttingSize(cs.getCuttingSize());
                this.processingNotBottomOrder(order, nextOrder, op, specs, cs.getTowardEdge());
                // test:
                this.actionService.completedAllMachineActions();
                synchronized (LOCK) {
                    while (!this.actionService.isAllMachineActionsCompleted()) {
                        LOCK.wait(WAIT_TIME);
                    }
                }
                this.processCompletedAction(order, BoardCategory.STOCK);
            }
        }
    }

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

    public void processCompletedAction(WorkOrder order, BoardCategory inventoryCategory) {
        int productCount = 0;
        Inventory inventory = null;
        int inventoryCount = 0;

        for (MachineAction action : this.actionService.getAllMachineActions()) {
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
