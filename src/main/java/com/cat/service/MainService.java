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
    WorkOrderService orderService;
    @Autowired
    MachineActionService actionService;
    @Autowired
    InventoryService inventoryService;
    @Autowired
    TrimmingValueService trimmingValueService;
    @Autowired
    StockSpecificationService stockSpecificationService;

    public void startService() throws InterruptedException {
        this.signalService.addNewSignal(SignalCategory.START_WORK);
        while (!this.signalService.isReceivedNewSignal(SignalCategory.START_WORK)) {
            Thread.sleep(3000);
        }

        OperatingParameter op = this.parameterService.getLatestOperatingParameter();
        TrimmingValue tv = this.trimmingValueService.getLatestTrimmingValue();
        List<StockSpecification> specs = this.stockSpecificationService.getGroupSpecification();

        List<WorkOrder> orders = this.orderService.getBottomOrders(op.getBottomOrderSort(), op.getWorkOrderDate());

        CutBoard legacyCutBoard = null;

        for (WorkOrder order : orders) {
            this.orderService.updateOrderState(order, OrderState.ALREADY_STARTED);
            while (order.getUnfinishedAmount() != 0) {
                legacyCutBoard = this.processingBottomOrder(order, legacyCutBoard, op, tv);
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
            NormalBoard nextProduct = null;
            if (i < orders.size() - 1) {
                WorkOrder nextOrder = orders.get(i + 1);
                nextProduct = new NormalBoard(nextOrder.getSpecStr(), nextOrder.getMaterial(), BoardCategory.PRODUCT);
            }
            this.orderService.updateOrderState(order, OrderState.ALREADY_STARTED);
            while (order.getUnfinishedAmount() != 0) {
                legacyCutBoard = this.processingNotBottomOrder(order, legacyCutBoard, nextProduct, op, tv, specs);
                this.signalService.addNewSignal(SignalCategory.ACTION);
                while (!this.signalService.isReceivedNewSignal(SignalCategory.ACTION)) {
                    Thread.sleep(3000);
                }
                this.actionService.processCompletedAction(order, BoardCategory.STOCK);
            }
        }
    }

    public CutBoard processingBottomOrder(WorkOrder order, CutBoard legacyCutBoard, OperatingParameter parameter, TrimmingValue trimmingValue) {
        String material = order.getMaterial();
        int orderId = order.getId();
        String orderModule = order.getSiteModule();
        BigDecimal fixedWidth = parameter.getFixedWidth();
        BigDecimal wasteThreshold = parameter.getWasteThreshold();
        List<BigDecimal> trimValues = trimmingValue.getTrimValues();

        logger.info("Order: {}", order);

        CutBoard orderCutBoard = new CutBoard(order.getCuttingSize(), material);
        logger.info("OrderCutBoard: {}", orderCutBoard);

        NormalBoard productBoard = this.boardService.getCanCutProduct(order.getSpecStr(), material, orderCutBoard.getWidth());
        logger.info("ProductBoard: {}", productBoard);

        CutBoard cutBoard = this.boardService.processingCutBoard(legacyCutBoard, orderCutBoard, productBoard, trimValues, wasteThreshold, orderId, orderModule);
        logger.info("processingCutBoard: {}", cutBoard);

        int productCutTimes = this.boardService.calProductCutTimes(cutBoard.getWidth(), productBoard.getWidth(), order.getUnfinishedAmount());
        logger.info("ProductCutTimes: {}", productCutTimes);

        NormalBoard semiProductBoard = new NormalBoard(cutBoard.getHeight(), fixedWidth, cutBoard.getLength(), material, BoardCategory.SEMI_PRODUCT);
        logger.info("SemiProductBoard: {}", semiProductBoard);

        int semiProductCutTimes = this.boardService.calNotProductCutTimes(cutBoard, productBoard.getWidth(), productCutTimes, semiProductBoard);
        logger.info("SemiProductCutTimes: {}", semiProductCutTimes);

        this.boardService.twoStep(cutBoard, semiProductBoard, semiProductCutTimes, wasteThreshold, orderId, orderModule);

        this.boardService.threeStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId, orderModule);

        return cutBoard.getWidth().compareTo(BigDecimal.ZERO) == 0 ? null : cutBoard;
    }

    public CutBoard processingNotBottomOrder(WorkOrder order, CutBoard legacyCutBoard, NormalBoard nextOrderProductBoard, OperatingParameter parameter, TrimmingValue trimmingValue, List<StockSpecification> specs) {
        String material = order.getMaterial();
        int orderId = order.getId();
        String orderModule = order.getSiteModule();
        BigDecimal wasteThreshold = parameter.getWasteThreshold();
        List<BigDecimal> trimValues = trimmingValue.getTrimValues();

        logger.info("Order: {}", order);

        CutBoard orderCutBoard = new CutBoard(order.getCuttingSize(), material);
        logger.info("OrderCutBoard: {}", orderCutBoard);

        NormalBoard productBoard = this.boardService.getCanCutProduct(order.getSpecStr(), material, orderCutBoard.getWidth());
        logger.info("ProductBoard: {}", productBoard);

        logger.info("legacyCutBoard: {}", legacyCutBoard);

        CutBoard cutBoard = this.boardService.processingCutBoard(legacyCutBoard, orderCutBoard, productBoard, trimValues, wasteThreshold, orderId, orderModule);
        logger.info("processingCutBoard: {}", cutBoard);

        int productCutTimes = this.boardService.calProductCutTimes(cutBoard.getWidth(), productBoard.getWidth(), order.getUnfinishedAmount());
        logger.info("ProductCutTimes: {}", productCutTimes);

        if (productCutTimes == order.getUnfinishedAmount()) {
            logger.info("order last time processing");
            BigDecimal remainingWidth = cutBoard.getWidth().subtract(productBoard.getWidth().multiply(new BigDecimal(productCutTimes)));
            NormalBoard remainingBoard = new NormalBoard(cutBoard.getHeight(), remainingWidth, productBoard.getLength(), material, BoardCategory.REMAINING);
            logger.info("remainingCutBoard: {}", remainingBoard);
            logger.info("nextOrderProductBoard: {}", nextOrderProductBoard);

            if (remainingBoard.compareTo(nextOrderProductBoard) >= 0) {
                logger.info("remainingCutBoard can reuse");

                this.boardService.twoStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId, orderModule);
            } else {
                logger.info("remainingCutBoard can't reuse");

                NormalBoard stockBoard = this.boardService.getMatchStockBoard(specs, cutBoard.getHeight(), material);
                logger.info("stockBoard: {}", stockBoard);

                int stockBoardCutTimes = this.boardService.calNotProductCutTimes(cutBoard, productBoard.getWidth(), productCutTimes, stockBoard);
                logger.info("stockBoardCutTimes: {}", stockBoardCutTimes);

                if (stockBoardCutTimes > 0) {
                    logger.info("can cutting stockBoard");

                    if (productBoard.getLength().compareTo(stockBoard.getLength()) >= 0) {
                        logger.info("first cutting productBoard");

                        this.boardService.twoStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId, orderModule);

                        this.boardService.threeStep(cutBoard, stockBoard, stockBoardCutTimes, wasteThreshold, orderId, orderModule);
                    } else {
                        logger.info("first cutting stockBoard");

                        this.boardService.twoStep(cutBoard, stockBoard, stockBoardCutTimes, wasteThreshold, orderId, orderModule);

                        this.boardService.threeStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId, orderModule);
                    }
                } else {
                    logger.info("can't cutting stockBoard");

                    this.boardService.threeStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId, orderModule);
                }
            }
        } else {
            logger.info("order not last time processing");

            this.boardService.threeStep(cutBoard, productBoard, productCutTimes, wasteThreshold, orderId, orderModule);
        }

        logger.info("CutBoard in the end: {}", cutBoard);

        return cutBoard.getWidth().compareTo(BigDecimal.ZERO) == 0 ? null : cutBoard;
    }
}
