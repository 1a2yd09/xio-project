package com.cat.service;

import com.cat.entity.*;
import com.cat.entity.enums.BoardCategory;
import com.cat.util.BoardUtil;
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
    WorkOrderService orderService;

    @Autowired
    InventoryService inventoryService;

    @Autowired
    ParameterService parameterService;

    public int calProductCutTimes(BigDecimal cutBoardWidth, BigDecimal productBoardWidth, Integer orderUnfinishedTimes) {
        // TODO: 如果能把板材裁剪次数也作为板材的属性之一，就可以在获得板材对象的同时计算板材的裁剪次数。可以从板材基类延申出一个非下料板类型的板材类。
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

    public void processBottomOrder(WorkOrder order) {
        // 如果是和处理工单对象有关的流程，要保证是从数据库中最新获取的，防止前面的流程有对相应的数据表写的操作:
        String material = order.getMaterial();
        int orderId = order.getId();
        String orderModule = order.getSiteModule();
        OperatingParameter op = this.parameterService.getLatestOperatingParameter();

        logger.info("Order: {}", order);

        CutBoard cutBoard = new CutBoard(order.getCuttingSize(), material, BoardCategory.CUTTING);
        this.boardService.pickingCutBoard(cutBoard, orderId, orderModule);
        logger.info("CutBoard: {}", cutBoard);

        this.boardService.trimmingCutBoard(cutBoard, this.parameterService.getTrimValues(), op.getWasteThreshold(), orderId, orderModule);
        logger.info("CutBoard after trimming: {}", cutBoard);

        Board productBoard = BoardUtil.getStandardBoard(order.getSpecification(), material, BoardCategory.PRODUCT);
        logger.info("ProductBoard: {}", productBoard);

        int productCutTimes = this.calProductCutTimes(cutBoard.getWidth(), productBoard.getWidth(), order.getUnfinishedAmount());
        logger.info("ProductCutTimes: {}", productCutTimes);

        Board semiProductBoard = new Board(cutBoard.getHeight(), op.getFixedWidth(), cutBoard.getLength(), material, BoardCategory.SEMI_PRODUCT);
        logger.info("SemiProductBoard: {}", semiProductBoard);

        int semiProductCutTimes = this.calNotProductCutTimes(cutBoard.getWidth(), productBoard.getWidth(), productCutTimes, semiProductBoard.getWidth());
        logger.info("SemiProductCutTimes: {}", semiProductCutTimes);

        this.boardService.cuttingTargetBoard(cutBoard, semiProductBoard, semiProductCutTimes, orderId, orderModule);
        logger.info("CutBoard after cuttingSemiBoard: {}", cutBoard);

        this.boardService.cuttingExtraLength(cutBoard, productBoard.getLength(), op.getWasteThreshold(), orderId, orderModule);
        logger.info("CutBoard after cuttingExtraLength: {}", cutBoard);

        this.boardService.cuttingExtraWidth(cutBoard, productBoard.getWidth().multiply(new BigDecimal(productCutTimes)), op.getWasteThreshold(), orderId, orderModule);
        logger.info("CutBoard after cuttingExtraWidth: {}", cutBoard);

        this.boardService.cuttingTargetBoard(cutBoard, productBoard, productCutTimes - 1, orderId, orderModule);
        logger.info("CutBoard after cuttingProductBoard: {}", cutBoard);

        this.boardService.sendingTargetBoard(productBoard, orderId, orderModule);
    }

    public void processFinishedAction(List<MachineAction> actions) {
        for (MachineAction action : actions) {
            // TODO: 可能需要判断动作是否已完成。
            String boardCategory = action.getBoardCategory();
            if (boardCategory.equals(BoardCategory.PRODUCT.value)) {
                // TODO: 可以改成统一记录，最后一次写入，现在是每有一个成品操作就写入一次数据表。
                this.orderService.addOrderCompletedAmount(action.getWorkOrderId(), 1);
            } else if (boardCategory.equals(BoardCategory.SEMI_PRODUCT.value)) {
                this.inventoryService.addInventory(action.getBoardSpecification(), action.getBoardMaterial(), 1, BoardCategory.SEMI_PRODUCT.value);
            }
        }
    }

    public CutBoard processNotBottomOrder(WorkOrder order, CutBoard legacyCutBoard) {
        String material = order.getMaterial();
        int orderId = order.getId();
        String orderModule = order.getSiteModule();

        CutBoard orderCutBoard = new CutBoard(order.getCuttingSize(), material, BoardCategory.CUTTING);
        Board productBoard = BoardUtil.getStandardBoard(order.getSpecification(), material, BoardCategory.PRODUCT);
        // TODO: 其实逻辑应该是这样，根据一个函数确定此次下料板是使用遗留板材还是工单下料板，然后进行后续流程。
        CutBoard cutBoard;
        if (legacyCutBoard == null) {
            // 无遗留板材，取工单下料板并修边:
            cutBoard = orderCutBoard;
        } else {
            if (legacyCutBoard.compareTo(productBoard) > 0) {
                // 有遗留板材，可用于工单成品裁剪，将遗留板材作为此次过程的下料板:
                cutBoard = legacyCutBoard;
            } else {
                // 有遗留板材，不可用于工单成品裁剪，推送遗留板材，取工单下料板并修边:
                this.boardService.sendingTargetBoard(legacyCutBoard, orderId, orderModule);
                cutBoard = orderCutBoard;
            }
        }

        return null;
    }
}
