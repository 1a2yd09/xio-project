package com.cat.service;

import com.cat.dao.ActionDao;
import com.cat.entity.bean.WorkOrder;
import com.cat.entity.board.CutBoard;
import com.cat.entity.board.NormalBoard;
import com.cat.entity.param.StockSpecification;
import com.cat.enums.ActionCategory;
import com.cat.enums.BoardCategory;
import com.cat.enums.ForwardEdge;
import com.cat.utils.Arith;
import com.cat.utils.BoardUtils;
import com.cat.utils.ParamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author CAT
 */
@Component
public class BoardService {
    @Autowired
    ActionDao actionDao;

    /**
     * 新增下料板旋转动作。
     *
     * @param cutBoard    下料板
     * @param forwardEdge 下料板旋转后的朝向
     * @param orderId     工单 ID
     */
    public void rotatingCutBoard(CutBoard cutBoard, ForwardEdge forwardEdge, Integer orderId) {
        // 如果下料板此时朝向和指定朝向不同，则旋转下料板:
        if (cutBoard.getForwardEdge() != forwardEdge) {
            this.actionDao.insertMachineAction(ActionCategory.ROTATE, cutBoard, orderId);
            cutBoard.setForwardEdge(forwardEdge);
        }
    }

    /**
     * 新增下料板裁剪动作。
     *
     * @param cutBoard    下料板
     * @param targetBoard 裁剪板
     * @param orderId     工单 ID
     */
    public void cuttingCutBoard(CutBoard cutBoard, NormalBoard targetBoard, Integer orderId) {
        for (int i = 0; i < targetBoard.getCutTimes(); i++) {
            BigDecimal dis = targetBoard.getWidth();
            // 下料板根据自身朝向从宽度或长度当中扣除进刀距离:
            if (cutBoard.getForwardEdge() == ForwardEdge.LONG) {
                cutBoard.setWidth(Arith.sub(cutBoard.getWidth(), dis));
            } else {
                cutBoard.setLength(Arith.sub(cutBoard.getLength(), dis));
            }
            // 如果下料板剩余宽度大于零，表示动作为裁剪动作，如果下料板剩余宽度等于零，则用送板动作替换裁剪动作:
            int res = Arith.cmp(cutBoard.getWidth(), BigDecimal.ZERO);
            if (res > 0) {
                this.actionDao.insertMachineAction(ActionCategory.CUT, dis, targetBoard, orderId);
            } else if (res == 0) {
                this.actionDao.insertMachineAction(ActionCategory.SEND, targetBoard, orderId);
            }
        }
    }

    /**
     * 指定下料板的裁剪方向并裁剪指定板材。
     *
     * @param cutBoard    下料板
     * @param forwardEdge 裁剪方向
     * @param targetBoard 指定板材
     * @param orderId     工单 ID
     */
    public void cuttingTargetBoard(CutBoard cutBoard, ForwardEdge forwardEdge, NormalBoard targetBoard, Integer orderId) {
        if (targetBoard.getCutTimes() > 0) {
            this.rotatingCutBoard(cutBoard, forwardEdge, orderId);
            this.cuttingCutBoard(cutBoard, targetBoard, orderId);
        }
    }

    /**
     * 指定下料板的裁剪方向并裁剪额外板材。
     *
     * @param cutBoard       下料板
     * @param forwardEdge    裁剪方向
     * @param targetMeasure  目标度量
     * @param wasteThreshold 废料阈值
     * @param orderId        工单 ID
     */
    public void cuttingExtraBoard(CutBoard cutBoard, ForwardEdge forwardEdge, BigDecimal targetMeasure, BigDecimal wasteThreshold, Integer orderId) {
        NormalBoard extraBoard = this.getExtraBoard(cutBoard, forwardEdge, targetMeasure, wasteThreshold);
        this.cuttingTargetBoard(cutBoard, forwardEdge, extraBoard, orderId);
    }

    /**
     * 预先板材裁剪。
     *
     * @param cutBoard       下料板
     * @param targetBoard    目标板
     * @param wasteThreshold 废料阈值
     * @param orderId        工单 ID
     */
    public void twoStep(CutBoard cutBoard, NormalBoard targetBoard, BigDecimal wasteThreshold, Integer orderId) {
        this.cuttingExtraBoard(cutBoard, ForwardEdge.SHORT, targetBoard.getLength(), wasteThreshold, orderId);
        this.cuttingTargetBoard(cutBoard, ForwardEdge.LONG, targetBoard, orderId);
    }

    /**
     * 后续板材裁剪。
     *
     * @param cutBoard       下料板
     * @param targetBoard    目标板
     * @param wasteThreshold 废料阈值
     * @param orderId        工单 ID
     */
    public void threeStep(CutBoard cutBoard, NormalBoard targetBoard, BigDecimal wasteThreshold, Integer orderId) {
        this.cuttingExtraBoard(cutBoard, ForwardEdge.SHORT, targetBoard.getLength(), wasteThreshold, orderId);
        this.cuttingExtraBoard(cutBoard, ForwardEdge.LONG, Arith.mul(targetBoard.getWidth(), targetBoard.getCutTimes()), wasteThreshold, orderId);
        this.cuttingTargetBoard(cutBoard, ForwardEdge.LONG, targetBoard, orderId);
    }

    /**
     * 获取下料板。
     *
     * @param cuttingSize 规格
     * @param material    材质
     * @param forwardEdge 朝向
     * @return 下料板
     */
    public CutBoard getCutBoard(String cuttingSize, String material, Integer forwardEdge) {
        if (forwardEdge == 1) {
            return new CutBoard(cuttingSize, material, ForwardEdge.LONG);
        } else {
            return new CutBoard(cuttingSize, material, ForwardEdge.SHORT);
        }
    }

    /**
     * 获取成品板。
     *
     * @param specification           规格
     * @param material                材质
     * @param cutBoardWidth           下料板宽度
     * @param orderIncompleteQuantity 工单未完成数目
     * @return 成品板
     */
    public NormalBoard getStandardProduct(String specification, String material, BigDecimal cutBoardWidth, Integer orderIncompleteQuantity) {
        NormalBoard product = new NormalBoard(specification, material, BoardCategory.PRODUCT);
        if (product.getWidth().compareTo(cutBoardWidth) > 0) {
            // 如果成品板宽度大于下料板宽度，则需要交换成品板的宽度和长度，不然会导致后续裁剪逻辑出错:
            BigDecimal tmp = product.getWidth();
            product.setWidth(product.getLength());
            product.setLength(tmp);
        }
        // 成品板的裁剪次数取决于最大裁剪次数以及工单未完成数目中的最小值:
        product.setCutTimes(Math.min(Arith.div(cutBoardWidth, product.getWidth()), orderIncompleteQuantity));
        return product;
    }

    /**
     * 获取半成品。
     *
     * @param cutBoard   下料板
     * @param fixedWidth 固定宽度
     * @param product    成品板
     * @return 半成品
     */
    public NormalBoard getSemiProduct(CutBoard cutBoard, BigDecimal fixedWidth, NormalBoard product) {
        NormalBoard semiProduct = new NormalBoard(cutBoard.getHeight(), fixedWidth, cutBoard.getLength(), cutBoard.getMaterial(), BoardCategory.SEMI_PRODUCT);
        if (semiProduct.getWidth().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal remainingWidth = Arith.sub(cutBoard.getWidth(), Arith.mul(product.getWidth(), product.getCutTimes()));
            // 半成品的裁剪次数取决于下料板裁剪成品后的剩余宽度以及半成品自身宽度:
            semiProduct.setCutTimes(Arith.div(remainingWidth, semiProduct.getWidth()));
        }
        return semiProduct;
    }

    /**
     * 获取库存件。
     *
     * @param specs    库存件规格集合
     * @param cutBoard 下料板
     * @param product  成品板
     * @return 库存件
     */
    public NormalBoard getMatchStock(List<StockSpecification> specs, CutBoard cutBoard, NormalBoard product) {
        StockSpecification ss = specs.stream()
                .filter(spec -> spec.getHeight().compareTo(cutBoard.getHeight()) == 0)
                .findFirst()
                .orElse(ParamUtils.getDefaultStockSpec());
        NormalBoard stock = new NormalBoard(ss.getHeight(), ss.getWidth(), ss.getLength(), cutBoard.getMaterial(), BoardCategory.STOCK);
        if (stock.getWidth().compareTo(BigDecimal.ZERO) > 0 && cutBoard.getLength().compareTo(stock.getLength()) > 0) {
            BigDecimal remainingWidth = Arith.sub(cutBoard.getWidth(), Arith.mul(product.getWidth(), product.getCutTimes()));
            // 库存件的裁剪次数取决于下料板裁剪成品后的剩余宽度以及库存件自身宽度:
            stock.setCutTimes(Arith.div(remainingWidth, stock.getWidth()));
        }
        return stock;
    }

    /**
     * 获取额外板材。
     *
     * @param cutBoard       下料板
     * @param forwardEdge    裁剪方向
     * @param targetMeasure  目标度量
     * @param wasteThreshold 废料阈值
     * @return 额外板材
     */
    public NormalBoard getExtraBoard(CutBoard cutBoard, ForwardEdge forwardEdge, BigDecimal targetMeasure, BigDecimal wasteThreshold) {
        NormalBoard extraBoard = new NormalBoard();
        extraBoard.setHeight(cutBoard.getHeight());
        // 以进刀出去的边作为较长边:
        if (forwardEdge == ForwardEdge.LONG) {
            extraBoard.setLength(cutBoard.getLength());
            extraBoard.setWidth(Arith.sub(cutBoard.getWidth(), targetMeasure));
        } else {
            extraBoard.setLength(cutBoard.getWidth());
            extraBoard.setWidth(Arith.sub(cutBoard.getLength(), targetMeasure));
        }
        extraBoard.setMaterial(cutBoard.getMaterial());
        extraBoard.setCategory(BoardUtils.calBoardCategory(extraBoard.getWidth(), extraBoard.getLength(), wasteThreshold));
        // 额外板材裁剪次数取决于目标度量和下料板对应度量的差值:
        extraBoard.setCutTimes(extraBoard.getWidth().compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
        return extraBoard;
    }

    /**
     * 获取后续成品。
     *
     * @param nextOrder    后续工单
     * @param currCutBoard 当前下料板
     * @param currProduct  当前成品板
     * @return 后续成品
     */
    public NormalBoard getNextProduct(WorkOrder nextOrder, CutBoard currCutBoard, NormalBoard currProduct) {
        NormalBoard nextProduct = new NormalBoard(nextOrder.getProductSpecification(), nextOrder.getMaterial(), BoardCategory.PRODUCT);
        if (currCutBoard.getMaterial().equals(nextProduct.getMaterial()) && currProduct.getLength().compareTo(nextProduct.getLength()) > 0) {
            BigDecimal remainingWidth = Arith.sub(currCutBoard.getWidth(), Arith.mul(currProduct.getWidth(), currProduct.getCutTimes()));
            // 后续成品的裁剪次数取决于最大裁剪次数以及工单未完成数目中的最小值:
            nextProduct.setCutTimes(Math.min(Arith.div(remainingWidth, nextProduct.getWidth()), nextOrder.getIncompleteQuantity()));
        }
        return nextProduct;
    }
}
