package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.CutBoard;
import com.cat.entity.MachineAction;
import com.cat.entity.WorkOrder;
import com.cat.entity.enums.BoardCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public void processBottomOrder(WorkOrder order) {
        // 如果是和处理工单对象有关的流程，要保证是从数据库中最新获取的，防止前面的流程有对相应的数据表写的操作:
        int orderId = order.getId();
        String orderModule = order.getSiteModule();
        logger.info("Order: {}", order);

        CutBoard cutBoard = this.boardService.getDefaultCutBoard(order.getCuttingSize(), order.getMaterial());
        this.boardService.pickingCutBoard(cutBoard, orderId, orderModule);
        logger.info("Picking CutBoard: {}", cutBoard);

        this.boardService.trimmingBoard(cutBoard, orderId, orderModule);
        logger.info("CutBoard after trimming: {}", cutBoard);

        Board productBoard = this.boardService.getStandardBoard(order.getSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
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
        int orderId = order.getId();
        String orderModule = order.getSiteModule();
        String material = order.getMaterial();

        CutBoard cutBoard = this.boardService.getDefaultCutBoard(order.getCuttingSize(), material);
        Board productBoard = this.boardService.getStandardBoard(order.getSpecification(), material, BoardCategory.PRODUCT);

        return null;
    }
}
