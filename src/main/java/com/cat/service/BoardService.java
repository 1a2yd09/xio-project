package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.CutBoard;
import com.cat.entity.enums.BoardCategory;
import com.cat.util.BoardUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BoardService {
    @Autowired
    MachineActionService actionService;

    public CutBoard pickCutBoard(String cuttingSize, String material, Integer orderId, String orderModule) {
        // TODO: 下料板是否也需要规格化。
        CutBoard cutBoard = new CutBoard(cuttingSize, material, BoardCategory.CUTTING, 0);
        this.actionService.addPickAction(cutBoard, orderId, orderModule);
        return cutBoard;
    }

    public void trimming(CutBoard cutBoard, List<BigDecimal> trimValues, Integer orderId, String orderModule) {
        // 下料板被抓取至工作台时，默认上边朝前:
        int currentForwardEdge = 0;
        for (int i = 0; i < trimValues.size(); i++) {
            BigDecimal trimValue = trimValues.get(i);
            if (trimValue.compareTo(BigDecimal.ZERO) > 0) {
                // 如果修边值不为零:
                for (int j = 0; j < i - currentForwardEdge; j++) {
                    // 根据该边和当前朝前边编码的差值进行相应次数的旋转操作:
                    this.actionService.addRotateAction(cutBoard, orderId, orderModule);
                }
                // 实例化一个废料板材对象，其中厚度为下料板厚度，宽度为修边值，材质为下料板材质，类型为废料:
                Board wastedBoard = new Board();
                wastedBoard.setHeight(cutBoard.getHeight());
                wastedBoard.setWidth(trimValue);
                wastedBoard.setMaterial(cutBoard.getMaterial());
                // TODO: 不能写死。
                wastedBoard.setCategory(BoardCategory.WASTED);
                if (i == 0 || i == 2) {
                    // 如果是上边和下边需要进刀，则废料板材的长度为下料板宽度:
                    wastedBoard.setLength(cutBoard.getWidth());
                    // 下料板的长度扣除该部分修边值:
                    cutBoard.setLength(cutBoard.getLength().subtract(trimValue));
                    // 设置下料板的朝前边为短边:
                    cutBoard.setForwardEdge(0);
                } else {
                    // 如果是左边和右边需要进刀，则废料板材的长度为下料板长度:
                    wastedBoard.setLength(cutBoard.getLength());
                    // 下料板的宽度扣除该部分修边值:
                    cutBoard.setWidth(cutBoard.getWidth().subtract(trimValue));
                    // 设置下料板的朝前边为长边:
                    cutBoard.setForwardEdge(1);
                }
                this.actionService.addCuttingAction(wastedBoard, orderId, orderModule);
                currentForwardEdge = i;
            }
        }
    }

    public Board getProductBoard(String specification, String material) {
        Board productBoard = new Board(specification, material, BoardCategory.PRODUCT);
        // 规格化成品板:
        BoardUtil.standardizingBoard(productBoard);
        return productBoard;
    }

    public int calProductBoardCutTimes(BigDecimal cuttingWidth, BigDecimal productWidth, Integer orderUnfinishedTimes) {
        int maxProductBoardCutTimes = cuttingWidth.divideToIntegralValue(productWidth).intValue();
        return Math.min(maxProductBoardCutTimes, orderUnfinishedTimes);
    }

    public int calSemiProductCutTimes(BigDecimal cuttingWidth, BigDecimal productWidth, int productCutTimes, BigDecimal fixedWidth) {
        BigDecimal remainingWidth = cuttingWidth.subtract(productWidth.multiply(new BigDecimal(productCutTimes)));
        // TODO: 固定宽度可能为零。
        return remainingWidth.divideToIntegralValue(fixedWidth).intValue();
    }

    public void cuttingBoardWidth(CutBoard cutBoard, Board targetBoard, int cutTimes, Integer orderId, String orderModule) {
        // TODO: 可能需要写成要进刀才旋转。
        if (cutBoard.getForwardEdge() == 0) {
            cutBoard.setForwardEdge(1);
            this.actionService.addRotateAction(cutBoard, orderId, orderModule);
        }
        for (int i = 0; i < cutTimes; i++) {
            cutBoard.setWidth(cutBoard.getWidth().subtract(targetBoard.getWidth()));
            this.actionService.addCuttingAction(targetBoard, orderId, orderModule);
        }
    }

    public void cuttingBoardLength(CutBoard cutBoard, Board targetBoard, int cutTimes, Integer orderId, String orderModule) {
        if (cutBoard.getForwardEdge() == 1) {
            cutBoard.setForwardEdge(0);
            this.actionService.addRotateAction(cutBoard, orderId, orderModule);
        }
        for (int i = 0; i < cutTimes; i++) {
            // TODO: 这里是长度减去裁剪板的宽度，就必须要求实例化的裁剪板严格按照逻辑来。
            cutBoard.setLength(cutBoard.getLength().subtract(targetBoard.getWidth()));
            this.actionService.addCuttingAction(targetBoard, orderId, orderModule);
        }
    }

    public void cuttingSemiBoard(CutBoard cutBoard, BigDecimal fixedWidth, int semiProductCutTimes, Integer orderId, String orderModule) {
        if (semiProductCutTimes > 0) {
            Board semiProductBoard = new Board(cutBoard.getHeight(), fixedWidth, cutBoard.getLength(), cutBoard.getMaterial(), BoardCategory.SEMI_PRODUCT);
            this.cuttingBoardWidth(cutBoard, semiProductBoard, semiProductCutTimes, orderId, orderModule);
        }
    }

    public void cuttingExtraLength(CutBoard cutBoard, Board targetBoard, BigDecimal wasteThreshold, Integer orderId, String orderModule) {
        BigDecimal extraLength = cutBoard.getLength().subtract(targetBoard.getLength());
        if (extraLength.compareTo(BigDecimal.ZERO) > 0) {
            Board extraBoard = new Board();
            extraBoard.setHeight(cutBoard.getHeight());
            extraBoard.setWidth(extraLength);
            extraBoard.setLength(cutBoard.getWidth());
            extraBoard.setMaterial(cutBoard.getMaterial());
            extraBoard.setCategory(BoardUtil.calBoardCategory(extraBoard.getWidth(), extraBoard.getLength(), wasteThreshold));
            this.cuttingBoardLength(cutBoard, extraBoard, 1, orderId, orderModule);
        }
    }

    public void cuttingExtraWidth(CutBoard cutBoard, Board targetBoard, int cutTimes, BigDecimal wasteThreshold, Integer orderId, String orderModule) {
        BigDecimal extraWidth = cutBoard.getWidth().subtract(targetBoard.getWidth().multiply(new BigDecimal(cutTimes)));
        if (extraWidth.compareTo(BigDecimal.ZERO) > 0) {
            Board extraBoard = new Board();
            extraBoard.setHeight(cutBoard.getHeight());
            extraBoard.setWidth(extraWidth);
            extraBoard.setLength(cutBoard.getLength());
            extraBoard.setMaterial(cutBoard.getMaterial());
            extraBoard.setCategory(BoardUtil.calBoardCategory(extraBoard.getWidth(), extraBoard.getLength(), wasteThreshold));
            this.cuttingBoardWidth(cutBoard, extraBoard, 1, orderId, orderModule);
        }
    }

    public void cuttingProductBoard(CutBoard cutBoard, Board targetBoard, int productCutTimes, Integer orderId, String orderModule) {
        if (productCutTimes > 1) {
            this.cuttingBoardWidth(cutBoard, targetBoard, productCutTimes - 1, orderId, orderModule);
        }
    }

    public void sendingBoard(Board targetBoard, Integer orderId, String orderModule) {
        this.actionService.addSendingAction(targetBoard, orderId, orderModule);
    }
}
