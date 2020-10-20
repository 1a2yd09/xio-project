package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.CutBoard;
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

    public void trimmingBoard(CutBoard cutBoard, Integer orderId, String orderModule) {
        List<BigDecimal> trimValues = parameterService.getTrimValues();
        logger.info("trimValues: {}", trimValues);
        BigDecimal wasteThreshold = parameterService.getLatestOperatingParameter().getWasteThreshold();
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

    public void pickingAndTrimmingCutBoard(CutBoard cutBoard, Integer orderId, String orderModule) {
        this.pickingCutBoard(cutBoard, orderId, orderModule);
        this.trimmingBoard(cutBoard, orderId, orderModule);
    }

    public void cuttingBoardExtraLength(CutBoard cutBoard, BigDecimal targetBoardLength, Integer orderId, String orderModule) {
        BigDecimal extraLength = cutBoard.getLength().subtract(targetBoardLength);
        if (extraLength.compareTo(BigDecimal.ZERO) > 0) {
            Board extraBoard = new Board();
            extraBoard.setHeight(cutBoard.getHeight());
            extraBoard.setWidth(extraLength);
            extraBoard.setLength(cutBoard.getWidth());
            extraBoard.setMaterial(cutBoard.getMaterial());
            BigDecimal wasteThreshold = parameterService.getLatestOperatingParameter().getWasteThreshold();
            extraBoard.setCategory(BoardUtil.calBoardCategory(extraBoard.getWidth(), extraBoard.getLength(), wasteThreshold));

            int rotateTimes = cutBoard.getForwardEdge() == 1 ? 1 : 0;
            this.rotatingBoard(cutBoard, rotateTimes, orderId, orderModule);
            this.cuttingBoard(cutBoard, extraBoard, 1, orderId, orderModule);
        }
    }

    public void cuttingBoardExtraWidth(CutBoard cutBoard, BigDecimal targetBoardWidth, int targetBoardCutTimes, Integer orderId, String orderModule) {
        BigDecimal extraWidth = cutBoard.getWidth().subtract(targetBoardWidth.multiply(new BigDecimal(targetBoardCutTimes)));
        if (extraWidth.compareTo(BigDecimal.ZERO) > 0) {
            Board extraBoard = new Board();
            extraBoard.setHeight(cutBoard.getHeight());
            extraBoard.setWidth(extraWidth);
            extraBoard.setLength(cutBoard.getLength());
            extraBoard.setMaterial(cutBoard.getMaterial());
            BigDecimal wasteThreshold = parameterService.getLatestOperatingParameter().getWasteThreshold();
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

    public void sendingBoard(Board targetBoard, Integer orderId, String orderModule) {
        this.actionService.addSendingAction(targetBoard, orderId, orderModule);
    }
}
