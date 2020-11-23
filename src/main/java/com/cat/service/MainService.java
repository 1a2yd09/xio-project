package com.cat.service;

import com.cat.entity.*;
import com.cat.entity.enums.ActionState;
import com.cat.entity.enums.BoardCategory;
import com.cat.entity.enums.OrderState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

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

    public void startService() throws InterruptedException {
        // test:
        this.signalService.insertStartSignal();
        while (!this.signalService.isReceivedNewStartSignal()) {
            Thread.sleep(3000);
        }

        OperatingParameter op = this.parameterService.getLatestOperatingParameter();
        List<StockSpecification> specs = this.stockSpecService.getGroupStockSpecs();

        List<WorkOrder> orders = this.orderService.getBottomOrders(op.getBottomOrderSort(), op.getWorkOrderDate());

        for (WorkOrder order : orders) {
            while (order.getUnfinishedAmount() != 0) {
                this.orderService.updateOrderState(order, OrderState.ALREADY_STARTED);
                this.signalService.insertTakeBoardSignal(order.getId());
                CuttingSignal cs = this.receiveCuttingSignal(order);
                order.setCuttingSize(cs.getCuttingSize());
                this.processingBottomOrder(order, op, cs.getTowardEdge());
                // test:
                this.actionService.completedAllMachineActions();
                while (!this.actionService.isAllMachineActionsCompleted()) {
                    Thread.sleep(3000);
                }
                this.processCompletedAction(order, BoardCategory.SEMI_PRODUCT);
            }
        }

        orders = orderService.getPreprocessNotBottomOrders(op.getWorkOrderDate());

        for (int i = 0; i < orders.size(); i++) {
            WorkOrder order = orders.get(i);
            WorkOrder nextOrder = null;
            if (i < orders.size() - 1) {
                nextOrder = orders.get(i + 1);
            }
            while (order.getUnfinishedAmount() != 0) {
                this.orderService.updateOrderState(order, OrderState.ALREADY_STARTED);
                this.signalService.insertTakeBoardSignal(order.getId());
                CuttingSignal cs = this.receiveCuttingSignal(order);
                order.setCuttingSize(cs.getCuttingSize());
                this.processingNotBottomOrder(order, nextOrder, op, specs, cs.getTowardEdge());
                // test:
                this.actionService.completedAllMachineActions();
                while (!this.actionService.isAllMachineActionsCompleted()) {
                    Thread.sleep(3000);
                }
                this.processCompletedAction(order, BoardCategory.STOCK);
            }
        }
    }

    public CuttingSignal receiveCuttingSignal(WorkOrder order) throws InterruptedException {
        this.signalService.insertCuttingSignal(order.getCuttingSize(), false, order.getId());
        while (true) {
            CuttingSignal cuttingSignal = this.signalService.getLatestNotProcessedCuttingSignal();
            if (cuttingSignal != null) {
                return cuttingSignal;
            }
            Thread.sleep(3000);
        }
    }

    public void processingBottomOrder(WorkOrder order, OperatingParameter parameter, Boolean cutBoardLongToward) {
        String material = order.getMaterial();
        int orderId = order.getId();
        BigDecimal fixedWidth = parameter.getFixedWidth();
        BigDecimal wasteThreshold = parameter.getWasteThreshold();

        CutBoard cutBoard = this.boardService.getCutBoard(order.getCuttingSize(), material, cutBoardLongToward);

        NormalBoard productBoard = this.boardService.getCanCutProduct(order.getSpecification(), material, cutBoard.getWidth());
        int productCutTimes = this.boardService.calProductCutTimes(cutBoard.getWidth(), productBoard.getWidth(), order.getUnfinishedAmount());

        NormalBoard semiProductBoard = new NormalBoard(cutBoard.getHeight(), fixedWidth, cutBoard.getLength(), material, BoardCategory.SEMI_PRODUCT);
        int semiProductCutTimes = this.boardService.calNotProductCutTimes(cutBoard, productBoard.getWidth(), productCutTimes, semiProductBoard);

        if (semiProductCutTimes > 0) {
            this.boardService.twoStep(cutBoard, semiProductBoard, semiProductCutTimes, wasteThreshold, orderId);
        }
        this.boardService.threeStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId);
    }

    public void processingNotBottomOrder(WorkOrder order, WorkOrder nextOrder, OperatingParameter parameter, List<StockSpecification> specs, Boolean cutBoardLongToward) {
        String material = order.getMaterial();
        int orderId = order.getId();
        BigDecimal wasteThreshold = parameter.getWasteThreshold();

        CutBoard cutBoard = this.boardService.getCutBoard(order.getCuttingSize(), material, cutBoardLongToward);

        NormalBoard productBoard = this.boardService.getCanCutProduct(order.getSpecification(), material, cutBoard.getWidth());
        int productCutTimes = this.boardService.calProductCutTimes(cutBoard.getWidth(), productBoard.getWidth(), order.getUnfinishedAmount());

        if (productCutTimes == order.getUnfinishedAmount()) {
            BigDecimal remainingWidth = cutBoard.getWidth().subtract(productBoard.getWidth().multiply(new BigDecimal(productCutTimes)));
            NormalBoard remainingBoard = new NormalBoard(cutBoard.getHeight(), remainingWidth, productBoard.getLength(), material, BoardCategory.REMAINING);

            NormalBoard nextProduct = this.boardService.getNextProduct(nextOrder);
            int nextOrderUnfinishedTimes = nextOrder == null ? 0 : nextOrder.getUnfinishedAmount();
            int nextProductCutTimes = this.boardService.calNextProductCutTimes(remainingBoard, nextProduct, nextOrderUnfinishedTimes);

            if (nextProductCutTimes > 0) {
                this.boardService.twoStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId);
                this.boardService.threeStep(cutBoard, nextProduct, nextProductCutTimes, wasteThreshold, nextOrder.getId());
            } else {
                NormalBoard stockBoard = this.boardService.getMatchStockBoard(specs, cutBoard.getHeight(), material);
                int stockBoardCutTimes = this.boardService.calNotProductCutTimes(cutBoard, productBoard.getWidth(), productCutTimes, stockBoard);

                if (stockBoardCutTimes > 0) {
                    if (productBoard.getLength().compareTo(stockBoard.getLength()) >= 0) {
                        this.boardService.twoStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId);
                        this.boardService.threeStep(cutBoard, stockBoard, stockBoardCutTimes, wasteThreshold, orderId);
                    } else {
                        this.boardService.twoStep(cutBoard, stockBoard, stockBoardCutTimes, wasteThreshold, orderId);
                        this.boardService.threeStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId);
                    }
                } else {
                    this.boardService.threeStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId);
                }
            }
        } else {
            this.boardService.threeStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId);
        }
    }

    public void processCompletedAction(WorkOrder order, BoardCategory inventoryCategory) {
        int productCount = 0;
        NormalBoard inventory = null;
        int inventoryCount = 0;

        for (MachineAction action : this.actionService.getAllMachineActions()) {
            if (ActionState.FINISHED.value.equals(action.getState())) {
                String boardCategory = action.getBoardCategory();
                if (BoardCategory.PRODUCT.value.equals(boardCategory)) {
                    productCount++;
                } else if (inventoryCategory.value.equals(boardCategory)) {
                    if (inventory == null) {
                        inventory = new NormalBoard(action.getBoardSpecification(), action.getBoardMaterial(), inventoryCategory);
                    }
                    inventoryCount++;
                }
            }
        }

        this.orderService.addOrderCompletedAmount(order, productCount);
        if (inventory != null) {
            this.inventoryService.addInventoryAmount(inventory, inventoryCount);
        }

        this.actionService.transferAllMachineActions();
        this.actionService.truncateMachineAction();
    }
}
