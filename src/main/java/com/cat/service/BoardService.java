package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.CutBoard;
import com.cat.entity.StockSpecification;
import com.cat.entity.enums.BoardCategory;
import com.cat.util.BoardUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BoardService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    MachineActionService actionService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    TrimmingValueService trimmingValueService;
    @Autowired
    StockSpecificationService stockSpecificationService;

    public void rotatingBoard(CutBoard cutBoard, int rotateTimes, Integer orderId, String orderModule) {
        for (int i = 0; i < rotateTimes; i++) {
            this.actionService.addRotateAction(cutBoard, orderId, orderModule);
            if (cutBoard.getForwardEdge() == 1) {
                cutBoard.setForwardEdge(0);
            } else {
                cutBoard.setForwardEdge(1);
            }
        }
    }

    public void cuttingBoard(CutBoard cutBoard, Board targetBoard, int cutTimes, Integer orderId, String orderModule) {
        for (int i = 0; i < cutTimes; i++) {
            this.actionService.addCuttingAction(targetBoard, orderId, orderModule);
            if (cutBoard.getForwardEdge() == 1) {
                cutBoard.setWidth(cutBoard.getWidth().subtract(targetBoard.getWidth()));
            } else {
                cutBoard.setLength(cutBoard.getLength().subtract(targetBoard.getWidth()));
            }
        }
    }

    public void pickingCutBoard(CutBoard cutBoard, Integer orderId, String orderModule) {
        this.actionService.addPickAction(cutBoard, orderId, orderModule);
    }

    public void trimmingCutBoard(CutBoard cutBoard, List<BigDecimal> trimValues, BigDecimal wasteThreshold, Integer orderId, String orderModule) {
        logger.info("trimValues: {}", trimValues);
        logger.info("wasteThreshold: {}", wasteThreshold);
        int currentForwardEdge = 0;
        for (int i = 0; i < trimValues.size(); i++) {
            BigDecimal trimValue = trimValues.get(i);
            if (trimValue.compareTo(BigDecimal.ZERO) > 0) {
                Board wastedBoard = new Board();
                wastedBoard.setHeight(cutBoard.getHeight());
                wastedBoard.setWidth(trimValue);
                wastedBoard.setMaterial(cutBoard.getMaterial());
                if (i == 0 || i == 2) {
                    wastedBoard.setLength(cutBoard.getWidth());
                } else {
                    wastedBoard.setLength(cutBoard.getLength());
                }
                wastedBoard.setCategory(BoardUtil.calBoardCategory(wastedBoard.getWidth(), wastedBoard.getLength(), wasteThreshold));

                this.rotatingBoard(cutBoard, i - currentForwardEdge, orderId, orderModule);
                this.cuttingBoard(cutBoard, wastedBoard, 1, orderId, orderModule);

                currentForwardEdge = i;
            }
        }
    }

    public void pickingAndTrimmingCutBoard(CutBoard cutBoard, List<BigDecimal> trimValues, BigDecimal wasteThreshold, Integer orderId, String orderModule) {
        this.pickingCutBoard(cutBoard, orderId, orderModule);
        this.trimmingCutBoard(cutBoard, trimValues, wasteThreshold, orderId, orderModule);
    }

    public void cuttingExtraLength(CutBoard cutBoard, BigDecimal targetLength, BigDecimal wasteThreshold, Integer orderId, String orderModule) {
        BigDecimal extraLength = cutBoard.getLength().subtract(targetLength);
        if (extraLength.compareTo(BigDecimal.ZERO) > 0) {
            Board extraBoard = new Board();
            extraBoard.setHeight(cutBoard.getHeight());
            extraBoard.setWidth(extraLength);
            extraBoard.setLength(cutBoard.getWidth());
            extraBoard.setMaterial(cutBoard.getMaterial());
            extraBoard.setCategory(BoardUtil.calBoardCategory(extraBoard.getWidth(), extraBoard.getLength(), wasteThreshold));

            int rotateTimes = cutBoard.getForwardEdge() == 1 ? 1 : 0;
            this.rotatingBoard(cutBoard, rotateTimes, orderId, orderModule);
            this.cuttingBoard(cutBoard, extraBoard, 1, orderId, orderModule);
        }
    }

    public void cuttingExtraWidth(CutBoard cutBoard, BigDecimal targetWidth, BigDecimal wasteThreshold, Integer orderId, String orderModule) {
        BigDecimal extraWidth = cutBoard.getWidth().subtract(targetWidth);
        if (extraWidth.compareTo(BigDecimal.ZERO) > 0) {
            Board extraBoard = new Board();
            extraBoard.setHeight(cutBoard.getHeight());
            extraBoard.setWidth(extraWidth);
            extraBoard.setLength(cutBoard.getLength());
            extraBoard.setMaterial(cutBoard.getMaterial());
            extraBoard.setCategory(BoardUtil.calBoardCategory(extraBoard.getWidth(), extraBoard.getLength(), wasteThreshold));

            int rotateTimes = cutBoard.getForwardEdge() == 1 ? 0 : 1;
            this.rotatingBoard(cutBoard, rotateTimes, orderId, orderModule);
            this.cuttingBoard(cutBoard, extraBoard, 1, orderId, orderModule);
        }
    }

    public void cuttingTargetBoard(CutBoard cutBoard, Board targetBoard, int cutTimes, Integer orderId, String orderModule) {
        if (cutTimes > 0) {
            int rotateTimes = cutBoard.getForwardEdge() == 1 ? 0 : 1;
            this.rotatingBoard(cutBoard, rotateTimes, orderId, orderModule);
            this.cuttingBoard(cutBoard, targetBoard, cutTimes, orderId, orderModule);
        }
    }

    public void sendingTargetBoard(CutBoard cutBoard, Board targetBoard, Integer orderId, String orderModule) {
        // TODO: 这里应该改成依旧送走的是下料板，但是此时类型不再是下料板，而是传入的类型。
        this.actionService.addSendingAction(targetBoard, orderId, orderModule);
        cutBoard.setWidth(BigDecimal.ZERO);
    }

    public CutBoard processingCutBoard(CutBoard legacyCutBoard, CutBoard orderCutBoard, Board productBoard, Integer orderId, String orderModule) {
        BigDecimal wasteThreshold = this.parameterService.getLatestOperatingParameter().getWasteThreshold();
        List<BigDecimal> trimValues = this.trimmingValueService.getTrimValues();

        if (legacyCutBoard == null) {
            // 无遗留板材，取工单下料板并修边:
            this.pickingAndTrimmingCutBoard(orderCutBoard, trimValues, wasteThreshold, orderId, orderModule);
            logger.info("Picking and trimming orderCutBoard: {}", orderCutBoard);
            return orderCutBoard;
        } else {
            if (legacyCutBoard.compareTo(productBoard) >= 0) {
                // 有遗留板材，可用于工单成品裁剪，将遗留板材作为此次过程的下料板，遗留板材不需要修边操作:
                logger.info("Using legacyCutBoard: {}", legacyCutBoard);
                return legacyCutBoard;
            } else {
                // 有遗留板材，不可用于工单成品裁剪，推送遗留板材，取工单下料板并修边:
                this.sendingTargetBoard(legacyCutBoard, legacyCutBoard, orderId, orderModule);
                logger.info("Sending legacyCutBoard");
                this.pickingAndTrimmingCutBoard(orderCutBoard, trimValues, wasteThreshold, orderId, orderModule);
                logger.info("Picking and trimming orderCutBoard: {}", orderCutBoard);
                return orderCutBoard;
            }
        }
    }

    public Board getStockBoard(BigDecimal height, String material) {
        StockSpecification ss = this.stockSpecificationService.getMatchSpecification(height);
        if (ss != null) {
            return new Board(ss.getHeight(), ss.getWidth(), ss.getLength(), material, BoardCategory.STOCK);
        } else {
            return new Board(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, material, BoardCategory.STOCK);
        }
    }

    public Board getCanCutProduct(BigDecimal orderCutBoardWidth, String specification, String material) {
        Board product = new Board(specification, material, BoardCategory.PRODUCT);
        if (product.getWidth().compareTo(orderCutBoardWidth) > 0) {
            BigDecimal tmp = product.getWidth();
            product.setWidth(product.getLength());
            product.setLength(tmp);
        }
        return product;
    }
}
