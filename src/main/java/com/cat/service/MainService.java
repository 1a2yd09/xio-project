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
    @Autowired
    MailService mailService;

    /**
     * 主流程。
     *
     * @throws InterruptedException 等待过程被中断
     */
    public void start() throws InterruptedException {
        this.signalService.waitingForNewStartSignal();

        OperatingParameter param = this.parameterService.getLatestOperatingParameter();
        List<StockSpecification> specs = this.stockSpecService.getGroupStockSpecs();
        // 轿底工单:
        List<WorkOrder> orders = this.orderService.getBottomOrders(param.getSortPattern(), param.getOrderDate());
        for (WorkOrder order : orders) {
            while (order.getIncompleteQuantity() != 0) {
                this.signalService.insertTakeBoardSignal(order.getId());
                CuttingSignal cuttingSignal = this.signalService.receiveNewCuttingSignal(order);
                this.processingBottomOrder(order, param, cuttingSignal);
                this.actionService.waitingForAllMachineActionsCompleted();
                this.processCompletedAction(order, BoardCategory.SEMI_PRODUCT);
            }
        }
        // 对重直梁工单:
        orders = orderService.getPreprocessNotBottomOrders(param.getOrderDate());
        for (int i = 0; i < orders.size(); i++) {
            WorkOrder currOrder = orders.get(i);
            WorkOrder nextOrder = OrderUtils.getFakeOrder();
            if (i < orders.size() - 1) {
                nextOrder = orders.get(i + 1);
            }
            while (currOrder.getIncompleteQuantity() != 0) {
                this.signalService.insertTakeBoardSignal(currOrder.getId());
                CuttingSignal cuttingSignal = this.signalService.receiveNewCuttingSignal(currOrder);
                this.processingNotBottomOrder(currOrder, nextOrder, param, specs, cuttingSignal);
                this.actionService.waitingForAllMachineActionsCompleted();
                this.processCompletedAction(currOrder, BoardCategory.STOCK);
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
        String material = order.getMaterial();
        int orderId = order.getId();
        BigDecimal fixedWidth = parameter.getFixedWidth();
        BigDecimal wasteThreshold = parameter.getWasteThreshold();

        CutBoard cutBoard = this.boardService.getCutBoard(cuttingSignal.getCuttingSize(), material, cuttingSignal.getForwardEdge());
        NormalBoard productBoard = this.boardService.getStandardProduct(order.getProductSpecification(), material, cutBoard.getWidth(), order.getIncompleteQuantity());
        NormalBoard semiProductBoard = this.boardService.getSemiProduct(cutBoard, fixedWidth, productBoard);

        this.boardService.twoStep(cutBoard, semiProductBoard, wasteThreshold, orderId);
        this.boardService.threeStep(cutBoard, productBoard, wasteThreshold, orderId);
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
        String material = order.getMaterial();
        int orderId = order.getId();
        BigDecimal wasteThreshold = parameter.getWasteThreshold();

        CutBoard cutBoard = this.boardService.getCutBoard(cuttingSignal.getCuttingSize(), material, cuttingSignal.getForwardEdge());
        NormalBoard productBoard = this.boardService.getStandardProduct(order.getProductSpecification(), material, cutBoard.getWidth(), order.getIncompleteQuantity());

        if (productBoard.getCutTimes() == order.getIncompleteQuantity()) {
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
     * 处理一组被机器处理完毕的动作。
     *
     * @param order             工单
     * @param inventoryCategory 存货类型
     */
    public void processCompletedAction(WorkOrder order, BoardCategory inventoryCategory) {
        int productCount = 0;
        Inventory inventory = null;
        int inventoryCount = 0;

        for (MachineAction action : this.actionService.getAllMachineActions()) {
            // 只处理动作状态为已完成的动作:
            if (ActionState.COMPLETED.value.equals(action.getState())) {
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

        this.orderService.addOrderCompletedQuantity(order, productCount);
        if (inventory != null) {
            inventory.setQuantity(inventoryCount);
            this.inventoryService.updateInventoryQuantity(inventory);
        }

        this.actionService.transferAllMachineActions();
        this.actionService.truncateMachineAction();
    }
}
