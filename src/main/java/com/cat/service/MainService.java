package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.CutBoard;
import com.cat.entity.WorkOrder;
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
        int orderId = order.getId();
        String orderModule = order.getSiteModule();
        logger.info("Order: {}", order);

        CutBoard cutBoard = this.boardService.pickingBoard(order.getCuttingSize(), order.getMaterial(), orderId, orderModule);
        logger.info("Picking CutBoard: {}", cutBoard);

        this.boardService.trimmingBoard(cutBoard, orderId, orderModule);
        logger.info("CutBoard after trimming: {}", cutBoard);

        Board productBoard = this.boardService.getStandardProductBoard(order.getSpecification(), order.getMaterial());
        logger.info("ProductBoard: {}", productBoard);

        int productCutTimes = this.boardService.calProductBoardCutTimes(cutBoard.getWidth(), productBoard.getWidth(), order.getUnfinishedAmount());
        logger.info("ProductCutTimes: {}", productCutTimes);

        int semiProductCutTimes = this.boardService.calSemiProductCutTimes(cutBoard.getWidth(), productBoard.getWidth(), productCutTimes);
        logger.info("SemiProductCutTimes: {}", semiProductCutTimes);

        Board semiProductBoard = this.boardService.getSemiProductBoard(cutBoard);
        logger.info("SemiProductBoard: {}", semiProductBoard);

        this.boardService.cuttingTargetBoard(cutBoard, semiProductBoard, semiProductCutTimes, orderId, orderModule);
        logger.info("CutBoard after cuttingSemiBoard: {}", cutBoard);

        this.boardService.cuttingBoardExtraLength(cutBoard, productBoard.getLength(), orderId, orderModule);
        logger.info("CutBoard after cuttingBoardExtraLength: {}", cutBoard);

        this.boardService.cuttingBoardExtraWidth(cutBoard, productBoard.getWidth(), productCutTimes, orderId, orderModule);
        logger.info("CutBoard after cuttingBoardExtraWidth: {}", cutBoard);

        this.boardService.cuttingTargetBoard(cutBoard, productBoard, productCutTimes - 1, orderId, orderModule);
        logger.info("CutBoard after cuttingProductBoard: {}", cutBoard);

        this.boardService.sendingBoard(productBoard, orderId, orderModule);
    }
}
