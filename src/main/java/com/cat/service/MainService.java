package com.cat.service;

import com.cat.entity.*;
import com.cat.entity.enums.BoardCategory;
import com.cat.entity.enums.OrderState;
import com.cat.entity.enums.SignalCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
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

    public void startService() throws InterruptedException {
        this.signalService.addNewSignal(SignalCategory.START_WORK);
        while (!this.signalService.isReceivedNewSignal(SignalCategory.START_WORK)) {
            Thread.sleep(3000);
        }

        OperatingParameter op = this.parameterService.getLatestOperatingParameter();
        List<StockSpecification> specs = this.stockSpecService.getGroupSpecs();

        List<WorkOrder> orders = this.orderService.getBottomOrders(op.getBottomOrderSort(), op.getWorkOrderDate());

        for (WorkOrder order : orders) {
            this.orderService.updateOrderState(order, OrderState.ALREADY_STARTED);
            while (order.getUnfinishedAmount() != 0) {
                this.processingBottomOrder(order, op);
                this.signalService.addNewSignal(SignalCategory.ACTION);
                while (!this.signalService.isReceivedNewSignal(SignalCategory.ACTION)) {
                    Thread.sleep(3000);
                }
                this.actionService.processCompletedAction(order, BoardCategory.SEMI_PRODUCT);
            }
        }

        orders = orderService.getPreprocessNotBottomOrders(op.getWorkOrderDate());

        for (int i = 0; i < orders.size(); i++) {
            WorkOrder order = orders.get(i);
            WorkOrder nextOrder = null;
            if (i < orders.size() - 1) {
                nextOrder = orders.get(i + 1);
            }
            this.orderService.updateOrderState(order, OrderState.ALREADY_STARTED);
            while (order.getUnfinishedAmount() != 0) {
                this.processingNotBottomOrder(order, nextOrder, op, specs);
                this.signalService.addNewSignal(SignalCategory.ACTION);
                while (!this.signalService.isReceivedNewSignal(SignalCategory.ACTION)) {
                    Thread.sleep(3000);
                }
                this.actionService.processCompletedAction(order, BoardCategory.STOCK);
            }
        }
    }

    public void processingBottomOrder(WorkOrder order, OperatingParameter parameter) {
        String material = order.getMaterial();
        int orderId = order.getId();
        String orderModule = order.getSiteModule();
        BigDecimal fixedWidth = parameter.getFixedWidth();
        BigDecimal wasteThreshold = parameter.getWasteThreshold();

        logger.debug("Order: {}", order);

        CutBoard cutBoard = new CutBoard(order.getCuttingSize(), material);
        logger.debug("OrderCutBoard: {}", cutBoard);

        NormalBoard productBoard = this.boardService.getCanCutProduct(order.getSpecification(), material, cutBoard.getWidth());
        logger.debug("ProductBoard: {}", productBoard);

        int productCutTimes = this.boardService.calProductCutTimes(cutBoard.getWidth(), productBoard.getWidth(), order.getUnfinishedAmount());
        logger.debug("ProductCutTimes: {}", productCutTimes);

        NormalBoard semiProductBoard = new NormalBoard(cutBoard.getHeight(), fixedWidth, cutBoard.getLength(), material, BoardCategory.SEMI_PRODUCT);
        logger.debug("SemiProductBoard: {}", semiProductBoard);

        int semiProductCutTimes = this.boardService.calNotProductCutTimes(cutBoard, productBoard.getWidth(), productCutTimes, semiProductBoard);
        logger.debug("SemiProductCutTimes: {}", semiProductCutTimes);

        this.boardService.twoStep(cutBoard, semiProductBoard, semiProductCutTimes, wasteThreshold, orderId, orderModule);

        this.boardService.threeStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId, orderModule);
    }

    public void processingNotBottomOrder(WorkOrder order, WorkOrder nextOrder, OperatingParameter parameter, List<StockSpecification> specs) {
        String material = order.getMaterial();
        int orderId = order.getId();
        String orderModule = order.getSiteModule();
        BigDecimal wasteThreshold = parameter.getWasteThreshold();

        logger.debug("Order: {}", order);

        CutBoard cutBoard = new CutBoard(order.getCuttingSize(), material);
        logger.debug("OrderCutBoard: {}", cutBoard);

        NormalBoard productBoard = this.boardService.getCanCutProduct(order.getSpecification(), material, cutBoard.getWidth());
        logger.debug("ProductBoard: {}", productBoard);

        int productCutTimes = this.boardService.calProductCutTimes(cutBoard.getWidth(), productBoard.getWidth(), order.getUnfinishedAmount());
        logger.debug("ProductCutTimes: {}", productCutTimes);

        if (productCutTimes == order.getUnfinishedAmount()) {
            logger.debug("order last time processing");

            BigDecimal remainingWidth = cutBoard.getWidth().subtract(productBoard.getWidth().multiply(new BigDecimal(productCutTimes)));
            NormalBoard remainingBoard = new NormalBoard(cutBoard.getHeight(), remainingWidth, productBoard.getLength(), material, BoardCategory.REMAINING);
            logger.debug("remainingCutBoard: {}", remainingBoard);

            NormalBoard nextProduct = this.boardService.getNextProduct(nextOrder);
            int nextOrderUnfinishedTimes = nextOrder == null ? 0 : nextOrder.getUnfinishedAmount();
            int nextProductCutTimes = this.boardService.calNextProductCutTimes(remainingBoard, nextProduct, nextOrderUnfinishedTimes);
            logger.debug("nextOrderProductBoard: {}", nextProduct);

            if (nextProductCutTimes > 0) {
                logger.debug("remainingCutBoard can reuse");

                this.boardService.twoStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId, orderModule);
                this.boardService.threeStep(cutBoard, nextProduct, nextProductCutTimes, wasteThreshold, nextOrder.getId(), nextOrder.getSiteModule());
            } else {
                logger.debug("remainingCutBoard can't reuse");

                NormalBoard stockBoard = this.boardService.getMatchStockBoard(specs, cutBoard.getHeight(), material);
                logger.debug("stockBoard: {}", stockBoard);

                int stockBoardCutTimes = this.boardService.calNotProductCutTimes(cutBoard, productBoard.getWidth(), productCutTimes, stockBoard);
                logger.debug("stockBoardCutTimes: {}", stockBoardCutTimes);

                if (stockBoardCutTimes > 0) {
                    logger.debug("can cutting stockBoard");

                    if (productBoard.getLength().compareTo(stockBoard.getLength()) >= 0) {
                        logger.debug("first cutting productBoard");

                        this.boardService.twoStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId, orderModule);

                        this.boardService.threeStep(cutBoard, stockBoard, stockBoardCutTimes, wasteThreshold, orderId, orderModule);
                    } else {
                        logger.debug("first cutting stockBoard");

                        this.boardService.twoStep(cutBoard, stockBoard, stockBoardCutTimes, wasteThreshold, orderId, orderModule);

                        this.boardService.threeStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId, orderModule);
                    }
                } else {
                    logger.debug("can't cutting stockBoard");

                    this.boardService.threeStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId, orderModule);
                }
            }
        } else {
            logger.debug("order not last time processing");

            this.boardService.threeStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId, orderModule);
        }
    }
}
