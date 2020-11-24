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

    public void cuttingCutBoard(CutBoard cutBoard, NormalBoard targetBoard, Integer orderId) {
        for (int i = 0; i < targetBoard.getCutTimes(); i++) {
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

    public void cuttingTargetBoard(CutBoard cutBoard, CutBoard.EdgeType forwardEdge, NormalBoard targetBoard, Integer orderId) {
        this.rotatingCutBoard(cutBoard, forwardEdge, orderId);
        this.cuttingCutBoard(cutBoard, targetBoard, orderId);
    }

    public void cuttingExtraBoard(CutBoard cutBoard, CutBoard.EdgeType forwardEdge, BigDecimal targetMeasure, BigDecimal wasteThreshold, Integer orderId) {
        NormalBoard extraBoard = this.getExtraBoard(cutBoard, forwardEdge, targetMeasure, wasteThreshold);
        if (extraBoard.getCutTimes() > 0) {
            this.cuttingTargetBoard(cutBoard, forwardEdge, extraBoard, orderId);
        }
    }

    public void twoStep(CutBoard cutBoard, NormalBoard targetBoard, BigDecimal wasteThreshold, Integer orderId) {
        this.cuttingExtraBoard(cutBoard, CutBoard.EdgeType.SHORT, targetBoard.getLength(), wasteThreshold, orderId);
        this.cuttingTargetBoard(cutBoard, CutBoard.EdgeType.LONG, targetBoard, orderId);
    }

    public void threeStep(CutBoard cutBoard, NormalBoard targetBoard, BigDecimal wasteThreshold, Integer orderId) {
        this.cuttingExtraBoard(cutBoard, CutBoard.EdgeType.SHORT, targetBoard.getLength(), wasteThreshold, orderId);
        this.cuttingExtraBoard(cutBoard, CutBoard.EdgeType.LONG, targetBoard.getWidth().multiply(new BigDecimal(targetBoard.getCutTimes())), wasteThreshold, orderId);
        this.cuttingTargetBoard(cutBoard, CutBoard.EdgeType.LONG, targetBoard, orderId);
    }

    public CutBoard getCutBoard(String cuttingSize, String material, Boolean cutBoardLongToward) {
        if (Boolean.TRUE.equals(cutBoardLongToward)) {
            return new CutBoard(cuttingSize, material, CutBoard.EdgeType.LONG);
        } else {
            return new CutBoard(cuttingSize, material, CutBoard.EdgeType.SHORT);
        }
    }

    public NormalBoard getStandardProduct(String specification, String material, BigDecimal cutBoardWidth, Integer orderUnfinishedAmount) {
        NormalBoard product = new NormalBoard(specification, material, BoardCategory.PRODUCT);
        if (product.getWidth().compareTo(cutBoardWidth) > 0) {
            // 如果成品板宽度大于下料板宽度，则需要交换成品板的宽度和长度，不然会导致裁剪逻辑出错:
            BigDecimal tmp = product.getWidth();
            product.setWidth(product.getLength());
            product.setLength(tmp);
        }
        product.setCutTimes(Math.min(cutBoardWidth.divideToIntegralValue(product.getWidth()).intValue(), orderUnfinishedAmount));
        return product;
    }

    public NormalBoard getSemiProduct(CutBoard cutBoard, BigDecimal fixedWidth, NormalBoard product) {
        NormalBoard semiProduct = new NormalBoard(cutBoard.getHeight(), fixedWidth, cutBoard.getLength(), cutBoard.getMaterial(), BoardCategory.SEMI_PRODUCT);
        if (semiProduct.getWidth().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal remainingWidth = cutBoard.getWidth().subtract(product.getWidth().multiply(new BigDecimal(product.getCutTimes())));
            semiProduct.setCutTimes(remainingWidth.divideToIntegralValue(semiProduct.getWidth()).intValue());
        }
        return semiProduct;
    }

    public NormalBoard getMatchStock(List<StockSpecification> specs, CutBoard cutBoard, NormalBoard product) {
        StockSpecification ss = specs.stream().filter(spec -> spec.getHeight().compareTo(cutBoard.getHeight()) == 0).findFirst().orElse(ParamUtil.getDefaultStockSpec());
        NormalBoard stock = new NormalBoard(ss.getHeight(), ss.getWidth(), ss.getLength(), cutBoard.getMaterial(), BoardCategory.STOCK);
        if (stock.getWidth().compareTo(BigDecimal.ZERO) > 0 && cutBoard.getLength().compareTo(stock.getLength()) > 0) {
            BigDecimal remainingWidth = cutBoard.getWidth().subtract(product.getWidth().multiply(new BigDecimal(product.getCutTimes())));
            stock.setCutTimes(remainingWidth.divideToIntegralValue(stock.getWidth()).intValue());
        }
        return stock;
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
        extraBoard.setCutTimes(extraBoard.getWidth().compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
        return extraBoard;
    }

    public NormalBoard getNextProduct(WorkOrder order, CutBoard cutBoard, NormalBoard currProduct) {
        NormalBoard nextProduct = new NormalBoard(order.getSpecification(), order.getMaterial(), BoardCategory.PRODUCT);
        if (cutBoard.getMaterial().equals(nextProduct.getMaterial()) && currProduct.getLength().compareTo(nextProduct.getLength()) > 0) {
            BigDecimal remainingWidth = cutBoard.getWidth().subtract(currProduct.getWidth().multiply(new BigDecimal(currProduct.getCutTimes())));
            nextProduct.setCutTimes(Math.min(remainingWidth.divideToIntegralValue(nextProduct.getWidth()).intValue(), order.getUnfinishedAmount()));
        }
        return nextProduct;
    }
}
