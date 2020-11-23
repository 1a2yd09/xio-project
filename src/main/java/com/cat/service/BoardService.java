package com.cat.service;

import com.cat.dao.ActionDao;
import com.cat.entity.CutBoard;
import com.cat.entity.NormalBoard;
import com.cat.entity.StockSpecification;
import com.cat.entity.WorkOrder;
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
    ActionDao actionDao;

    public void rotatingCutBoard(CutBoard cutBoard, CutBoard.EdgeType forwardEdge, Integer orderId) {
        if (cutBoard.getForwardEdge() != forwardEdge) {
            this.actionDao.insertMachineAction(ActionCategory.ROTATE, BigDecimal.ZERO, cutBoard, orderId);
            cutBoard.setForwardEdge(forwardEdge);
        }
    }

    public void cuttingCutBoard(CutBoard cutBoard, NormalBoard targetBoard, int cutTimes, Integer orderId) {
        for (int i = 0; i < cutTimes; i++) {
            BigDecimal dis = targetBoard.getWidth();
            if (cutBoard.getForwardEdge() == CutBoard.EdgeType.LONG) {
                cutBoard.setWidth(cutBoard.getWidth().subtract(dis));
            } else {
                cutBoard.setLength(cutBoard.getLength().subtract(dis));
            }
            if (cutBoard.getWidth().compareTo(BigDecimal.ZERO) > 0) {
                this.actionDao.insertMachineAction(ActionCategory.CUT, dis, targetBoard, orderId);
            } else if (cutBoard.getWidth().compareTo(BigDecimal.ZERO) == 0) {
                this.actionDao.insertMachineAction(ActionCategory.SEND, BigDecimal.ZERO, targetBoard, orderId);
            }
        }
    }

    public void cuttingTargetBoard(CutBoard cutBoard, CutBoard.EdgeType forwardEdge, NormalBoard targetBoard, int cutTimes, Integer orderId) {
        this.rotatingCutBoard(cutBoard, forwardEdge, orderId);
        this.cuttingCutBoard(cutBoard, targetBoard, cutTimes, orderId);
    }

    public void cuttingExtraBoard(CutBoard cutBoard, CutBoard.EdgeType forwardEdge, BigDecimal targetMeasure, BigDecimal wasteThreshold, Integer orderId) {
        NormalBoard extraBoard = this.getExtraBoard(cutBoard, forwardEdge, targetMeasure, wasteThreshold);
        // 如果下料板的边长已经等于目标边长，那就不需要旋转以及裁剪:
        if (extraBoard.getWidth().compareTo(BigDecimal.ZERO) > 0) {
            this.cuttingTargetBoard(cutBoard, forwardEdge, extraBoard, 1, orderId);
        }
    }

    public void twoStep(CutBoard cutBoard, NormalBoard targetBoard, int cutTimes, BigDecimal wasteThreshold, Integer orderId) {
        this.cuttingExtraBoard(cutBoard, CutBoard.EdgeType.SHORT, targetBoard.getLength(), wasteThreshold, orderId);
        this.cuttingTargetBoard(cutBoard, CutBoard.EdgeType.LONG, targetBoard, cutTimes, orderId);
    }

    public void threeStep(CutBoard cutBoard, NormalBoard targetBoard, int cutTimes, BigDecimal wasteThreshold, Integer orderId) {
        this.cuttingExtraBoard(cutBoard, CutBoard.EdgeType.SHORT, targetBoard.getLength(), wasteThreshold, orderId);
        this.cuttingExtraBoard(cutBoard, CutBoard.EdgeType.LONG, targetBoard.getWidth().multiply(new BigDecimal(cutTimes)), wasteThreshold, orderId);
        this.cuttingTargetBoard(cutBoard, CutBoard.EdgeType.LONG, targetBoard, cutTimes, orderId);
    }

    public NormalBoard getExtraBoard(CutBoard cutBoard, CutBoard.EdgeType forwardEdge, BigDecimal targetMeasure, BigDecimal wasteThreshold) {
        NormalBoard extraBoard = new NormalBoard();
        extraBoard.setHeight(cutBoard.getHeight());
        // 每次以出去的边作为较长边:
        if (forwardEdge == CutBoard.EdgeType.LONG) {
            extraBoard.setLength(cutBoard.getLength());
            extraBoard.setWidth(cutBoard.getWidth().subtract(targetMeasure));
        } else {
            extraBoard.setLength(cutBoard.getWidth());
            extraBoard.setWidth(cutBoard.getLength().subtract(targetMeasure));
        }
        extraBoard.setMaterial(cutBoard.getMaterial());
        extraBoard.setCategory(BoardUtil.calBoardCategory(extraBoard.getWidth(), extraBoard.getLength(), wasteThreshold));
        return extraBoard;
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
