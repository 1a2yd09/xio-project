package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.CutBoard;
import com.cat.entity.OperatingParameter;
import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BoardCategory;
import com.cat.entity.enums.SignalCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    @PostConstruct
    public void init() {
        this.actionService.truncateCompletedAction();
        this.actionService.truncateAction();
        this.inventoryService.truncateInventory();
        this.signalService.truncateSignal();
        this.orderService.truncateOrderTable();
        LocalDate orderDate = this.parameterService.getOperatingParameter().getWorkOrderDate();
        orderService.copyRemoteOrderToLocal(orderDate);
    }

    public void startService() throws InterruptedException {
        this.signalService.addNewSignal(SignalCategory.START_WORK);
        while (true) {
            if (this.signalService.isReceivedNewSignal(SignalCategory.START_WORK)) {
                logger.info("Received new start signal!!!");
                break;
            }
            logger.info("Not received new start signal...");
            Thread.sleep(3000);
        }

        OperatingParameter op = this.parameterService.getOperatingParameter();
        List<WorkOrder> orders = this.orderService.getBottomOrders(op.getBottomOrderSort(), op.getWorkOrderDate());

        for (WorkOrder order : orders) {
            while (order.getUnfinishedAmount() != 0) {
                this.processingBottomOrder(order, op.getFixedWidth(), op.getWasteThreshold());
                this.signalService.addNewSignal(SignalCategory.ACTION);
                while (true) {
                    if (this.signalService.isReceivedNewSignal(SignalCategory.ACTION)) {
                        logger.info("Received new action signal!!!");
                        this.actionService.processCompletedAction(order);
                        break;
                    }
                    logger.info("Not received new action signal...");
                    Thread.sleep(3000);
                }
            }
        }

        orders = orderService.getPreprocessNotBottomOrders(op.getWorkOrderDate());
        CutBoard legacyCutBoard = null;

        for (int i = 0; i < orders.size(); i++) {
            WorkOrder order = orders.get(i);
            Board nextProduct = null;
            if (i < orders.size() - 1) {
                WorkOrder nextOrder = orders.get(i + 1);
                nextProduct = new Board(nextOrder.getSpecification(), nextOrder.getMaterial(), BoardCategory.PRODUCT);
            }
            while (order.getUnfinishedAmount() != 0) {
                legacyCutBoard = this.processingNotBottomOrder(order, legacyCutBoard, nextProduct, op.getWasteThreshold());
                this.signalService.addNewSignal(SignalCategory.ACTION);
                while (true) {
                    if (this.signalService.isReceivedNewSignal(SignalCategory.ACTION)) {
                        logger.info("Received new action signal!!!");
                        this.actionService.processCompletedAction(order);
                        break;
                    }
                    logger.info("Not received new action signal...");
                    Thread.sleep(3000);
                }
            }
        }
    }

    public int calProductCutTimes(BigDecimal cutBoardWidth, BigDecimal productBoardWidth, Integer orderUnfinishedTimes) {
        int maxProductBoardCutTimes = cutBoardWidth.divideToIntegralValue(productBoardWidth).intValue();
        return Math.min(maxProductBoardCutTimes, orderUnfinishedTimes);
    }

    public int calNotProductCutTimes(BigDecimal cutBoardWidth, BigDecimal productBoardWidth, int productCutTimes, BigDecimal notProductWidth) {
        if (notProductWidth.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal remainingWidth = cutBoardWidth.subtract(productBoardWidth.multiply(new BigDecimal(productCutTimes)));
            return remainingWidth.divideToIntegralValue(notProductWidth).intValue();
        } else {
            return 0;
        }
    }

    public void processingBottomOrder(WorkOrder order, BigDecimal fixedWidth, BigDecimal wasteThreshold) {
        // 如果是和处理工单对象有关的流程，要保证是从数据库中最新获取的，防止前面的流程有对相应的数据表写的操作:
        String material = order.getMaterial();
        int orderId = order.getId();
        String orderModule = order.getSiteModule();

        logger.info("Order: {}", order);

        CutBoard orderCutBoard = new CutBoard(order.getCuttingSize(), material, BoardCategory.CUTTING);
        logger.info("OrderCutBoard: {}", orderCutBoard);

        Board productBoard = this.boardService.getCanCutProduct(orderCutBoard.getWidth(), order.getSpecification(), material);
        logger.info("ProductBoard: {}", productBoard);

        CutBoard cutBoard = this.boardService.processingCutBoard(null, orderCutBoard, productBoard, orderId, orderModule);
        logger.info("processingCutBoard: {}", cutBoard);

        int productCutTimes = this.calProductCutTimes(cutBoard.getWidth(), productBoard.getWidth(), order.getUnfinishedAmount());
        logger.info("ProductCutTimes: {}", productCutTimes);

        Board semiProductBoard = new Board(cutBoard.getHeight(), fixedWidth, cutBoard.getLength(), material, BoardCategory.SEMI_PRODUCT);
        logger.info("SemiProductBoard: {}", semiProductBoard);

        int semiProductCutTimes = this.calNotProductCutTimes(cutBoard.getWidth(), productBoard.getWidth(), productCutTimes, semiProductBoard.getWidth());
        logger.info("SemiProductCutTimes: {}", semiProductCutTimes);

        this.boardService.cuttingTargetBoard(cutBoard, semiProductBoard, semiProductCutTimes, orderId, orderModule);
        logger.info("CutBoard after cuttingSemiBoard: {}", cutBoard);

        this.boardService.cuttingExtraLength(cutBoard, productBoard.getLength(), wasteThreshold, orderId, orderModule);
        logger.info("CutBoard after cuttingExtraLength: {}", cutBoard);

        this.boardService.cuttingExtraWidth(cutBoard, productBoard.getWidth().multiply(new BigDecimal(productCutTimes)), wasteThreshold, orderId, orderModule);
        logger.info("CutBoard after cuttingExtraWidth: {}", cutBoard);

        this.boardService.cuttingTargetBoard(cutBoard, productBoard, productCutTimes - 1, orderId, orderModule);
        logger.info("CutBoard after cuttingProductBoard: {}", cutBoard);

        this.boardService.sendingTargetBoard(cutBoard, productBoard, orderId, orderModule);
        logger.info("CutBoard after sendingTargetBoard: {}", cutBoard);
    }

    public CutBoard processingNotBottomOrder(WorkOrder order, CutBoard legacyCutBoard, Board nextOrderProductBoard, BigDecimal wasteThreshold) {
        String material = order.getMaterial();
        int orderId = order.getId();
        String orderModule = order.getSiteModule();

        logger.info("Order: {}", order);

        CutBoard orderCutBoard = new CutBoard(order.getCuttingSize(), material, BoardCategory.CUTTING);
        logger.info("OrderCutBoard: {}", orderCutBoard);

        Board productBoard = this.boardService.getCanCutProduct(orderCutBoard.getWidth(), order.getSpecification(), material);
        logger.info("ProductBoard: {}", productBoard);

        logger.info("legacyCutBoard: {}", legacyCutBoard);

        CutBoard cutBoard = this.boardService.processingCutBoard(legacyCutBoard, orderCutBoard, productBoard, orderId, orderModule);
        logger.info("processingCutBoard: {}", cutBoard);

        int productCutTimes = this.calProductCutTimes(cutBoard.getWidth(), productBoard.getWidth(), order.getUnfinishedAmount());
        logger.info("ProductCutTimes: {}", productCutTimes);

        if (productCutTimes == order.getUnfinishedAmount()) {
            logger.info("order last time processing");
            BigDecimal remainingWidth = cutBoard.getWidth().subtract(productBoard.getWidth().multiply(new BigDecimal(productCutTimes)));
            CutBoard remainingCutBoard = new CutBoard(cutBoard.getHeight(), remainingWidth, productBoard.getLength(), material, BoardCategory.CUTTING);
            logger.info("remainingCutBoard: {}", remainingCutBoard);
            logger.info("nextOrderProductBoard: {}", nextOrderProductBoard);

            if (remainingCutBoard.compareTo(nextOrderProductBoard) >= 0) {
                logger.info("remainingCutBoard can reuse");

                this.boardService.cuttingExtraLength(cutBoard, productBoard.getLength(), wasteThreshold, orderId, orderModule);
                logger.info("CutBoard after cuttingExtraLength: {}", cutBoard);

                this.boardService.cuttingTargetBoard(cutBoard, productBoard, productCutTimes, orderId, orderModule);
                logger.info("CutBoard after cuttingProductBoard: {}", cutBoard);
            } else {
                logger.info("remainingCutBoard can't reuse");

                Board stockBoard = this.boardService.getMatchStockBoard(cutBoard.getHeight(), material);
                logger.info("stockBoard: {}", stockBoard);

                int stockBoardCutTimes = this.calNotProductCutTimes(cutBoard.getWidth(), productBoard.getWidth(), productCutTimes, stockBoard.getWidth());
                logger.info("stockBoardCutTimes: {}", stockBoardCutTimes);

                if (stockBoardCutTimes > 0) {
                    logger.info("can cutting stockBoard");

                    if (productBoard.getLength().compareTo(stockBoard.getLength()) >= 0) {
                        logger.info("first cutting productBoard");

                        this.boardService.cuttingExtraLength(cutBoard, productBoard.getLength(), wasteThreshold, orderId, orderModule);
                        logger.info("CutBoard after cuttingExtraLength: {}", cutBoard);

                        this.boardService.cuttingTargetBoard(cutBoard, productBoard, productCutTimes, orderId, orderModule);
                        logger.info("CutBoard after cuttingProductBoard: {}", cutBoard);

                        this.boardService.cuttingExtraLength(cutBoard, stockBoard.getLength(), wasteThreshold, orderId, orderModule);
                        logger.info("CutBoard after cuttingExtraLength: {}", cutBoard);

                        this.boardService.cuttingExtraWidth(cutBoard, stockBoard.getWidth().multiply(new BigDecimal(stockBoardCutTimes)), wasteThreshold, orderId, orderModule);
                        logger.info("CutBoard after cuttingExtraWidth: {}", cutBoard);

                        this.boardService.cuttingTargetBoard(cutBoard, stockBoard, stockBoardCutTimes - 1, orderId, orderModule);
                        logger.info("CutBoard after cuttingStockBoard: {}", cutBoard);

                        this.boardService.sendingTargetBoard(cutBoard, stockBoard, orderId, orderModule);
                    } else {
                        logger.info("first cutting stockBoard");

                        this.boardService.cuttingExtraLength(cutBoard, stockBoard.getLength(), wasteThreshold, orderId, orderModule);
                        logger.info("CutBoard after cuttingExtraLength: {}", cutBoard);

                        this.boardService.cuttingTargetBoard(cutBoard, stockBoard, stockBoardCutTimes, orderId, orderModule);
                        logger.info("CutBoard after cuttingStockBoard: {}", cutBoard);

                        this.boardService.cuttingExtraLength(cutBoard, productBoard.getLength(), wasteThreshold, orderId, orderModule);
                        logger.info("CutBoard after cuttingExtraLength: {}", cutBoard);

                        this.boardService.cuttingExtraWidth(cutBoard, productBoard.getWidth().multiply(new BigDecimal(productCutTimes)), wasteThreshold, orderId, orderModule);
                        logger.info("CutBoard after cuttingExtraWidth: {}", cutBoard);

                        this.boardService.cuttingTargetBoard(cutBoard, productBoard, productCutTimes - 1, orderId, orderModule);
                        logger.info("CutBoard after cuttingProductBoard: {}", cutBoard);

                        this.boardService.sendingTargetBoard(cutBoard, productBoard, orderId, orderModule);
                    }
                } else {
                    logger.info("can't cutting stockBoard");

                    this.boardService.cuttingExtraLength(cutBoard, productBoard.getLength(), wasteThreshold, orderId, orderModule);
                    logger.info("CutBoard after cuttingExtraLength: {}", cutBoard);

                    this.boardService.cuttingExtraWidth(cutBoard, productBoard.getWidth().multiply(new BigDecimal(productCutTimes)), wasteThreshold, orderId, orderModule);
                    logger.info("CutBoard after cuttingExtraWidth: {}", cutBoard);

                    this.boardService.cuttingTargetBoard(cutBoard, productBoard, productCutTimes - 1, orderId, orderModule);
                    logger.info("CutBoard after cuttingProductBoard: {}", cutBoard);

                    this.boardService.sendingTargetBoard(cutBoard, productBoard, orderId, orderModule);
                }
            }
        } else {
            logger.info("order not last time processing");

            this.boardService.cuttingExtraLength(cutBoard, productBoard.getLength(), wasteThreshold, orderId, orderModule);
            logger.info("CutBoard after cuttingExtraLength: {}", cutBoard);

            this.boardService.cuttingExtraWidth(cutBoard, productBoard.getWidth().multiply(new BigDecimal(productCutTimes)), wasteThreshold, orderId, orderModule);
            logger.info("CutBoard after cuttingExtraWidth: {}", cutBoard);

            this.boardService.cuttingTargetBoard(cutBoard, productBoard, productCutTimes - 1, orderId, orderModule);
            logger.info("CutBoard after cuttingProductBoard: {}", cutBoard);

            this.boardService.sendingTargetBoard(cutBoard, productBoard, orderId, orderModule);
        }

        logger.info("CutBoard in the end: {}", cutBoard);
        // TODO: 既然送板会将下料板的宽度置零，那实际就不需要对遗留板材进行空判断，直接进行板材比较即可。
        return cutBoard.getWidth().compareTo(BigDecimal.ZERO) == 0 ? null : cutBoard;
    }
}
