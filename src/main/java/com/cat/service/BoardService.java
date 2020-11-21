package com.cat.service;

import com.cat.entity.*;
import com.cat.entity.enums.ActionCategory;
import com.cat.entity.enums.BoardCategory;
import com.cat.util.BoardUtil;
import com.cat.util.ParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BoardService {
    @Autowired
    ActionService actionService;

    public void rotatingCutBoard(CutBoard cutBoard, int rotateTimes, Integer orderId) {
        for (int i = 0; i < rotateTimes; i++) {
            this.actionService.addAction(ActionCategory.ROTATE, BigDecimal.ZERO, cutBoard, orderId);
            if (cutBoard.getForwardEdge() == CutBoard.EdgeType.LONG) {
                cutBoard.setForwardEdge(CutBoard.EdgeType.SHORT);
            } else {
                cutBoard.setForwardEdge(CutBoard.EdgeType.LONG);
            }
        }
    }

    public void cuttingCutBoard(CutBoard cutBoard, NormalBoard targetBoard, int cutTimes, Integer orderId) {
        for (int i = 0; i < cutTimes; i++) {
            this.actionService.addAction(ActionCategory.CUT, targetBoard.getWidth(), targetBoard, orderId);
            if (cutBoard.getForwardEdge() == CutBoard.EdgeType.LONG) {
                cutBoard.setWidth(cutBoard.getWidth().subtract(targetBoard.getWidth()));
            } else {
                cutBoard.setLength(cutBoard.getLength().subtract(targetBoard.getWidth()));
            }
        }
    }

    public void cuttingExtraLength(CutBoard cutBoard, BigDecimal targetLength, BigDecimal wasteThreshold, Integer orderId) {
        BigDecimal extraLength = cutBoard.getLength().subtract(targetLength);
        if (extraLength.compareTo(BigDecimal.ZERO) > 0) {
            NormalBoard extraBoard = new NormalBoard();
            extraBoard.setHeight(cutBoard.getHeight());
            extraBoard.setWidth(extraLength);
            extraBoard.setLength(cutBoard.getWidth());
            extraBoard.setMaterial(cutBoard.getMaterial());
            extraBoard.setCategory(BoardUtil.calBoardCategory(extraBoard.getWidth(), extraBoard.getLength(), wasteThreshold));

            int rotateTimes = cutBoard.getForwardEdge() == CutBoard.EdgeType.LONG ? 1 : 0;
            this.rotatingCutBoard(cutBoard, rotateTimes, orderId);
            this.cuttingCutBoard(cutBoard, extraBoard, 1, orderId);
        }
    }

    public void cuttingExtraWidth(CutBoard cutBoard, BigDecimal targetWidth, BigDecimal wasteThreshold, Integer orderId) {
        BigDecimal extraWidth = cutBoard.getWidth().subtract(targetWidth);
        if (extraWidth.compareTo(BigDecimal.ZERO) > 0) {
            NormalBoard extraBoard = new NormalBoard();
            extraBoard.setHeight(cutBoard.getHeight());
            extraBoard.setWidth(extraWidth);
            extraBoard.setLength(cutBoard.getLength());
            extraBoard.setMaterial(cutBoard.getMaterial());
            extraBoard.setCategory(BoardUtil.calBoardCategory(extraBoard.getWidth(), extraBoard.getLength(), wasteThreshold));

            int rotateTimes = cutBoard.getForwardEdge() == CutBoard.EdgeType.LONG ? 0 : 1;
            this.rotatingCutBoard(cutBoard, rotateTimes, orderId);
            this.cuttingCutBoard(cutBoard, extraBoard, 1, orderId);
        }
    }

    public void cuttingTargetBoard(CutBoard cutBoard, NormalBoard targetBoard, int cutTimes, Integer orderId) {
        if (cutTimes > 0) {
            int rotateTimes = cutBoard.getForwardEdge() == CutBoard.EdgeType.LONG ? 0 : 1;
            this.rotatingCutBoard(cutBoard, rotateTimes, orderId);
            this.cuttingCutBoard(cutBoard, targetBoard, cutTimes, orderId);
        }
        if (cutBoard.getWidth().compareTo(targetBoard.getWidth()) == 0) {
            this.sendingTargetBoard(cutBoard, targetBoard, orderId);
        }
    }

    public void sendingTargetBoard(CutBoard cutBoard, BaseBoard targetBoard, Integer orderId) {
        this.actionService.addAction(ActionCategory.SEND, BigDecimal.ZERO, targetBoard, orderId);
        cutBoard.setWidth(BigDecimal.ZERO);
    }

    public void twoStep(CutBoard cutBoard, NormalBoard targetBoard, int cutTimes, BigDecimal wasteThreshold, Integer orderId) {
        this.cuttingExtraLength(cutBoard, targetBoard.getLength(), wasteThreshold, orderId);
        this.cuttingTargetBoard(cutBoard, targetBoard, cutTimes, orderId);
    }

    public void threeStep(CutBoard cutBoard, NormalBoard targetBoard, int cutTimes, BigDecimal wasteThreshold, Integer orderId) {
        this.cuttingExtraLength(cutBoard, targetBoard.getLength(), wasteThreshold, orderId);
        this.cuttingExtraWidth(cutBoard, targetBoard.getWidth().multiply(new BigDecimal(cutTimes)), wasteThreshold, orderId);
        this.cuttingTargetBoard(cutBoard, targetBoard, cutTimes - 1, orderId);
    }

    public CutBoard getCutBoard(String cuttingSize, String material, Boolean cutBoardLongToward) {
        if (Boolean.TRUE.equals(cutBoardLongToward)) {
            return new CutBoard(cuttingSize, material, CutBoard.EdgeType.LONG);
        } else {
            return new CutBoard(cuttingSize, material, CutBoard.EdgeType.SHORT);
        }
    }

    public NormalBoard getMatchStockBoard(List<StockSpecification> specs, BigDecimal height, String material) {
        StockSpecification ss = specs.stream().filter(spec -> spec.getHeight().compareTo(height) == 0).findFirst().orElse(ParamUtil.getDefaultStockSpec());
        return new NormalBoard(ss.getHeight(), ss.getWidth(), ss.getLength(), material, BoardCategory.STOCK);
    }

    public NormalBoard getCanCutProduct(String specification, String material, BigDecimal orderCutBoardWidth) {
        NormalBoard product = new NormalBoard(specification, material, BoardCategory.PRODUCT);
        if (product.getWidth().compareTo(orderCutBoardWidth) > 0) {
            // 如果成品板宽度大于下料板宽度，则需要交换成品板的宽度和长度，不然会导致裁剪逻辑出错:
            BigDecimal tmp = product.getWidth();
            product.setWidth(product.getLength());
            product.setLength(tmp);
        }
        return product;
    }

    public NormalBoard getNextProduct(WorkOrder order) {
        if (order != null) {
            return new NormalBoard(order.getSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
        }
        return BoardUtil.getDefaultProduct();
    }

    public int calNextProductCutTimes(NormalBoard remainingBoard, NormalBoard nextProduct, Integer nextOrderUnfinishedTimes) {
        if (nextProduct.getWidth().compareTo(BigDecimal.ZERO) > 0 && remainingBoard.getLength().compareTo(nextProduct.getLength()) >= 0 && remainingBoard.getMaterial().equals(nextProduct.getMaterial())) {
            int maxCutTimes = remainingBoard.getWidth().divideToIntegralValue(nextProduct.getWidth()).intValue();
            return Math.min(maxCutTimes, nextOrderUnfinishedTimes);
        } else {
            return 0;
        }
    }

    public int calProductCutTimes(BigDecimal cutBoardWidth, BigDecimal productBoardWidth, Integer orderUnfinishedTimes) {
        int maxProductBoardCutTimes = cutBoardWidth.divideToIntegralValue(productBoardWidth).intValue();
        return Math.min(maxProductBoardCutTimes, orderUnfinishedTimes);
    }

    public int calNotProductCutTimes(CutBoard cutBoard, BigDecimal productBoardWidth, int productCutTimes, NormalBoard notProductBoard) {
        // 一、固定宽度和库存规格宽度可能为零，二、库存规格长度可能超出下料板长度:
        if (notProductBoard.getWidth().compareTo(BigDecimal.ZERO) > 0 && cutBoard.getLength().compareTo(notProductBoard.getLength()) >= 0) {
            BigDecimal remainingWidth = cutBoard.getWidth().subtract(productBoardWidth.multiply(new BigDecimal(productCutTimes)));
            return remainingWidth.divideToIntegralValue(notProductBoard.getWidth()).intValue();
        } else {
            return 0;
        }
    }
}
