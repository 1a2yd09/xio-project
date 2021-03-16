package com.cat.service;

import com.cat.enums.ActionCategory;
import com.cat.enums.BoardCategory;
import com.cat.enums.ForwardEdge;
import com.cat.mapper.ActionMapper;
import com.cat.pojo.*;
import com.cat.utils.ArithmeticUtil;
import com.cat.utils.BoardUtil;
import com.cat.utils.ParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author CAT
 */
@Service
public class BoardService {
    @Autowired
    ActionMapper actionMapper;

    /**
     * 板材裁剪函数，负责下料板材的实际旋转和裁剪操作。
     *
     * @param cutBoard    下料板
     * @param forwardEdge 朝向
     * @param targetBoard 目标板材
     */
    public void cuttingBoard(CutBoard cutBoard, ForwardEdge forwardEdge, NormalBoard targetBoard) {
        if (targetBoard.getCutTimes() > 0) {
            if (cutBoard.getForwardEdge() != forwardEdge) {
                cutBoard.setForwardEdge(forwardEdge);
                this.actionMapper.insertMachineAction(MachineAction.of(ActionCategory.ROTATE, BigDecimal.ZERO, cutBoard));
            }

            BigDecimal dis = targetBoard.getWidth();
            if (cutBoard.getForwardEdge() == ForwardEdge.LONG) {
                cutBoard.setWidth(ArithmeticUtil.sub(cutBoard.getWidth(), dis));
            } else {
                cutBoard.setLength(ArithmeticUtil.sub(cutBoard.getLength(), dis));
            }

            if (cutBoard.getWidth().compareTo(BigDecimal.ZERO) > 0) {
                this.actionMapper.insertMachineAction(MachineAction.of(ActionCategory.CUT, dis, targetBoard));
            } else {
                this.actionMapper.insertMachineAction(MachineAction.of(ActionCategory.SEND, BigDecimal.ZERO, targetBoard));
            }
        }
    }

    /**
     * 板材裁剪流程，负责定义下料板整个裁剪流程。
     *
     * @param cutBoard       下料板
     * @param boardList      目标板材列表
     * @param wasteThreshold 废料阈值
     */
    public void newCutting(CutBoard cutBoard, BoardList boardList, BigDecimal wasteThreshold) {
        List<NormalBoard> boards = boardList.getBoards();
        NormalBoard lastNormalBoard = boards.get(boards.size() - 1);
        if (BoardUtil.isAllowBackToFront(lastNormalBoard.getNormalBoardAllWidth(), lastNormalBoard.getWidth())) {
            NormalBoard extraBoard = this.getExtraBoard(cutBoard, ForwardEdge.LONG, boardList.getBoardAllWidth(), wasteThreshold);
            this.cuttingBoard(cutBoard, ForwardEdge.LONG, extraBoard);
            for (NormalBoard board : boards) {
                if (board.getCutTimes() > 0) {
                    extraBoard = this.getExtraBoard(cutBoard, ForwardEdge.SHORT, board.getLength(), wasteThreshold);
                    this.cuttingBoard(cutBoard, ForwardEdge.SHORT, extraBoard);
                }
                for (int i = 0; i < board.getCutTimes(); i++) {
                    this.cuttingBoard(cutBoard, ForwardEdge.LONG, board);
                }
            }
        } else {
            this.newestCutting(cutBoard, boardList, wasteThreshold);
        }
    }

    /**
     * 板材裁剪流程，负责定义下料板整个裁剪流程。
     *
     * @param cutBoard       下料板
     * @param boardList      目标板材列表
     * @param wasteThreshold 废料阈值
     */
    public void newestCutting(CutBoard cutBoard, BoardList boardList, BigDecimal wasteThreshold) {
        for (NormalBoard board : boardList.getBoards()) {
            if (board.getCutTimes() > 0) {
                NormalBoard extraBoard = this.getExtraBoard(cutBoard, ForwardEdge.SHORT, board.getLength(), wasteThreshold);
                this.cuttingBoard(cutBoard, ForwardEdge.SHORT, extraBoard);
            }
            for (int i = 0; i < board.getCutTimes(); i++) {
                BigDecimal remainingWidth = ArithmeticUtil.sub(cutBoard.getWidth(), board.getWidth());
                if (remainingWidth.compareTo(BoardUtil.CLAMP_DEPTH) <= 0) {
                    NormalBoard extraBoard = this.getExtraBoard(cutBoard, ForwardEdge.LONG, board.getWidth(), wasteThreshold);
                    this.cuttingBoard(cutBoard, ForwardEdge.LONG, extraBoard);
                }
                this.cuttingBoard(cutBoard, ForwardEdge.LONG, board);
            }
        }
        if (cutBoard.getWidth().compareTo(BigDecimal.ZERO) > 0) {
            NormalBoard extraBoard = this.getExtraBoard(cutBoard, ForwardEdge.LONG, BigDecimal.ZERO, wasteThreshold);
            this.cuttingBoard(cutBoard, ForwardEdge.LONG, extraBoard);
        }
    }

    /**
     * 获取下料板。
     *
     * @param cuttingSize 规格
     * @param material    材质
     * @param forwardEdge 朝向
     * @return 下料板
     */
    public CutBoard getCutBoard(String cuttingSize, String material, Integer orderId, Integer forwardEdge) {
        return new CutBoard(cuttingSize, material, orderId, forwardEdge == 1 ? ForwardEdge.LONG : ForwardEdge.SHORT);
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
    public NormalBoard getStandardProduct(String specification, String material, BigDecimal cutBoardWidth, Integer orderIncompleteQuantity, Integer orderId) {
        NormalBoard product = new NormalBoard(specification, material, BoardCategory.PRODUCT, orderId);
        if (product.getWidth().compareTo(cutBoardWidth) > 0) {
            // 如果成品板宽度大于下料板宽度，则需要交换成品板的宽度和长度，不然会导致后续裁剪逻辑出错:
            BigDecimal tmp = product.getWidth();
            product.setWidth(product.getLength());
            product.setLength(tmp);
        }
        // 成品板的裁剪次数取决于最大裁剪次数以及工单未完成数目中的最小值:
        product.setCutTimes(Math.min(ArithmeticUtil.div(BoardUtil.getAvailableWidth(cutBoardWidth, product.getWidth()), product.getWidth()), orderIncompleteQuantity));
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
        NormalBoard semiProduct = new NormalBoard(cutBoard.getHeight(), fixedWidth, cutBoard.getLength(), cutBoard.getMaterial(), BoardCategory.SEMI_PRODUCT, cutBoard.getOrderId());
        if (semiProduct.getWidth().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal productAllWidth = ArithmeticUtil.mul(product.getWidth(), product.getCutTimes());
            productAllWidth = ArithmeticUtil.cmp(product.getWidth(), BoardUtil.CLAMP_DEPTH) >= 0 ? productAllWidth : productAllWidth.add(BoardUtil.CLAMP_DEPTH);
            productAllWidth = BoardUtil.processPostProductAllWidth(productAllWidth, product.getLength(), semiProduct.getLength());
            BigDecimal remainingWidth = ArithmeticUtil.sub(cutBoard.getWidth(), productAllWidth);
            // 半成品的裁剪次数取决于下料板裁剪成品后的剩余宽度以及半成品自身宽度:
            semiProduct.setCutTimes(ArithmeticUtil.div(remainingWidth, semiProduct.getWidth()));
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
                .orElse(ParamUtil.getDefaultStockSpec());
        NormalBoard stock = new NormalBoard(ss.getHeight(), ss.getWidth(), ss.getLength(), cutBoard.getMaterial(), BoardCategory.STOCK, cutBoard.getOrderId());
        if (stock.getWidth().compareTo(BigDecimal.ZERO) > 0 && cutBoard.getLength().compareTo(stock.getLength()) > 0) {
            BigDecimal productAllWidth = ArithmeticUtil.mul(product.getWidth(), product.getCutTimes());
            BigDecimal remainingWidth = ArithmeticUtil.sub(cutBoard.getWidth(), productAllWidth);
            if (product.getLength().compareTo(stock.getLength()) >= 0) {
                if (BoardUtil.isAllowCutting(remainingWidth, product.getLength(), stock.getLength())) {
                    stock.setCutTimes(ArithmeticUtil.div(BoardUtil.getAvailableWidth(remainingWidth, stock.getWidth()), stock.getWidth()));
                }
            } else {
                productAllWidth = ArithmeticUtil.cmp(product.getWidth(), BoardUtil.CLAMP_DEPTH) >= 0 ? productAllWidth : productAllWidth.add(BoardUtil.CLAMP_DEPTH);
                productAllWidth = BoardUtil.processPostProductAllWidth(productAllWidth, product.getLength(), stock.getLength());
                remainingWidth = ArithmeticUtil.sub(cutBoard.getWidth(), productAllWidth);
                stock.setCutTimes(ArithmeticUtil.div(remainingWidth, stock.getWidth()));
            }
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
            extraBoard.setWidth(ArithmeticUtil.sub(cutBoard.getWidth(), targetMeasure));
        } else {
            extraBoard.setLength(cutBoard.getWidth());
            extraBoard.setWidth(ArithmeticUtil.sub(cutBoard.getLength(), targetMeasure));
        }
        extraBoard.setMaterial(cutBoard.getMaterial());
        extraBoard.setCategory(BoardUtil.calBoardCategory(extraBoard.getWidth(), extraBoard.getLength(), wasteThreshold));
        extraBoard.setOrderId(cutBoard.getOrderId());
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
        NormalBoard nextProduct = new NormalBoard(nextOrder.getProductSpecification(), nextOrder.getMaterial(), BoardCategory.PRODUCT, nextOrder.getId());
        if (currProduct.getMaterial().equals(nextProduct.getMaterial())) {
            BigDecimal remainingWidth = ArithmeticUtil.sub(currCutBoard.getWidth(), ArithmeticUtil.mul(currProduct.getWidth(), currProduct.getCutTimes()));
            if (BoardUtil.isAllowCutting(remainingWidth, currProduct.getLength(), nextProduct.getLength())) {
                nextProduct.setCutTimes(Math.min(ArithmeticUtil.div(BoardUtil.getAvailableWidth(remainingWidth, nextProduct.getWidth()), nextProduct.getWidth()), nextOrder.getIncompleteQuantity()));
            }
        }
        return nextProduct;
    }
}
