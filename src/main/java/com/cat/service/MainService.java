package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.CutBoard;
import com.cat.entity.OperatingParameter;
import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BoardCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MainService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    MachineActionService actionService;
    @Autowired
    BoardService boardService;
    @Autowired
    ParameterService parameterService;

    public void processBottomOrder(WorkOrder order) {
        logger.info("Order: {}", order);
        CutBoard cutBoard = this.boardService.pickCutBoard(order.getCuttingSize(), order.getMaterial(), order.getId(), order.getSiteModule());
        logger.info("CutBoard before trimming: {}", cutBoard);
        this.boardService.trimming(cutBoard, parameterService.getTrimValues(), order.getId(), order.getSiteModule());
        logger.info("CutBoard after trimming: {}", cutBoard);
        Board productBoard = this.boardService.getProductBoard(order.getSpecification(), order.getMaterial());
        logger.info("ProductBoard: {}", productBoard);
        int productCutTimes = this.boardService.calProductBoardCutTimes(cutBoard.getWidth(), productBoard.getWidth(), order.getUnfinishedAmount());
        logger.info("productCutTimes: {}", productCutTimes);
        OperatingParameter op = this.parameterService.getLatestOperatingParameter();
        logger.info("FixedWidth: {}", op.getFixedWidth());
        int semiProductCutTimes = this.boardService.calSemiProductCutTimes(cutBoard.getWidth(), productBoard.getWidth(), productCutTimes, op.getFixedWidth());
        logger.info("semiProductCutTimes: {}", semiProductCutTimes);
        this.boardService.cuttingSemiBoard(cutBoard, op.getFixedWidth(), semiProductCutTimes, order.getId(), order.getSiteModule());
        logger.info("CutBoard after cuttingSemiBoard: {}", cutBoard);
        this.boardService.cuttingExtraLength(cutBoard, productBoard, op.getWasteThreshold(), order.getId(), order.getSiteModule());
        logger.info("CutBoard after cuttingExtraLength: {}", cutBoard);
        this.boardService.cuttingExtraWidth(cutBoard, productBoard, productCutTimes, op.getWasteThreshold(), order.getId(), order.getSiteModule());
        logger.info("CutBoard after cuttingExtraWidth: {}", cutBoard);
        this.boardService.cuttingProductBoard(cutBoard, productBoard, productCutTimes, order.getId(), order.getSiteModule());
        logger.info("CutBoard after cuttingProductBoard: {}", cutBoard);
        this.boardService.sendingBoard(productBoard, order.getId(), order.getSiteModule());
    }
}
