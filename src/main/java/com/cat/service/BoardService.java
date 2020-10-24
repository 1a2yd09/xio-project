package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.CutBoard;
import com.cat.entity.StockSpecification;
import com.cat.entity.enums.ActionCategory;
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

    public void rotatingCutBoard(CutBoard cutBoard, int rotateTimes, Integer orderId, String orderModule) {
        for (int i = 0; i < rotateTimes; i++) {
            this.actionService.addAction(ActionCategory.ROTATE, BigDecimal.ZERO, cutBoard, orderId, orderModule);
            cutBoard.changeForwardEdge();
        }
    }

    public void cuttingCutBoard(CutBoard cutBoard, Board targetBoard, int cutTimes, Integer orderId, String orderModule) {
        for (int i = 0; i < cutTimes; i++) {
            this.actionService.addAction(ActionCategory.CUT, targetBoard.getWidth(), targetBoard, orderId, orderModule);
            cutBoard.modifySpec(targetBoard.getWidth());
        }
    }

    public void pickingAndTrimmingCutBoard(CutBoard cutBoard, List<BigDecimal> trimValues, BigDecimal wasteThreshold, Integer orderId, String orderModule) {
        this.actionService.addAction(ActionCategory.PICK, BigDecimal.ZERO, cutBoard, orderId, orderModule);

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

                this.rotatingCutBoard(cutBoard, i - currentForwardEdge, orderId, orderModule);
                this.cuttingCutBoard(cutBoard, wastedBoard, 1, orderId, orderModule);

                currentForwardEdge = i;
            }
        }
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
            this.rotatingCutBoard(cutBoard, rotateTimes, orderId, orderModule);
            this.cuttingCutBoard(cutBoard, extraBoard, 1, orderId, orderModule);
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
            this.rotatingCutBoard(cutBoard, rotateTimes, orderId, orderModule);
            this.cuttingCutBoard(cutBoard, extraBoard, 1, orderId, orderModule);
        }
    }

    public void cuttingTargetBoard(CutBoard cutBoard, Board targetBoard, int cutTimes, Integer orderId, String orderModule) {
        if (cutTimes > 0) {
            int rotateTimes = cutBoard.getForwardEdge() == 1 ? 0 : 1;
            this.rotatingCutBoard(cutBoard, rotateTimes, orderId, orderModule);
            this.cuttingCutBoard(cutBoard, targetBoard, cutTimes, orderId, orderModule);
        }
    }

    public void sendingTargetBoard(CutBoard cutBoard, Board targetBoard, Integer orderId, String orderModule) {
        this.actionService.addAction(ActionCategory.SEND, BigDecimal.ZERO, targetBoard, orderId, orderModule);
        cutBoard.setWidth(BigDecimal.ZERO);
    }

    public CutBoard processingCutBoard(CutBoard legacyCutBoard, CutBoard orderCutBoard, Board productBoard, Integer orderId, String orderModule) {
        BigDecimal wasteThreshold = this.parameterService.getOperatingParameter().getWasteThreshold();
        List<BigDecimal> trimValues = this.trimmingValueService.getTrimValues();

        if (legacyCutBoard == null) {
            this.pickingAndTrimmingCutBoard(orderCutBoard, trimValues, wasteThreshold, orderId, orderModule);
            logger.info("Picking and trimming orderCutBoard: {}", orderCutBoard);
            return orderCutBoard;
        } else {
            if (legacyCutBoard.compareTo(productBoard) >= 0) {
                logger.info("Using legacyCutBoard: {}", legacyCutBoard);
                return legacyCutBoard;
            } else {
                this.sendingTargetBoard(legacyCutBoard, legacyCutBoard, orderId, orderModule);
                logger.info("Sending legacyCutBoard");
                this.pickingAndTrimmingCutBoard(orderCutBoard, trimValues, wasteThreshold, orderId, orderModule);
                logger.info("Picking and trimming orderCutBoard: {}", orderCutBoard);
                return orderCutBoard;
            }
        }
    }

    public Board getMatchStockBoard(BigDecimal height, String material) {
        StockSpecification ss = this.stockSpecificationService.getMatchSpecification(height);
        return new Board(ss.getHeight(), ss.getWidth(), ss.getLength(), material, BoardCategory.STOCK);
    }

    public Board getCanCutProduct(BigDecimal orderCutBoardWidth, String specification, String material) {
        Board product = new Board(specification, material, BoardCategory.PRODUCT);
        if (product.getWidth().compareTo(orderCutBoardWidth) > 0) {
            // 如果成品板宽度大于下料板宽度，则需要交换成品板的宽度和长度，不然会导致裁剪逻辑出错:
            BigDecimal tmp = product.getWidth();
            product.setWidth(product.getLength());
            product.setLength(tmp);
        }
        return product;
    }
}
