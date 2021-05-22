package com.cat.utils;

import com.cat.enums.BoardCategory;
import com.cat.enums.ForwardEdge;
import com.cat.pojo.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author CAT
 */
public class BoardUtil {
    /**
     * 夹钳宽度
     */
    public static final BigDecimal CLAMP_WIDTH = new BigDecimal(480);
    /**
     * 夹钳深度
     */
    public static final BigDecimal CLAMP_DEPTH = new BigDecimal(245);
    /**
     * 规格字符串分隔符
     */
    public static final String SPEC_SEP = "×";
    /**
     * 规格度量精度
     */
    public static final int DEC_SCALE = 2;

    private BoardUtil() {
    }

    /**
     * 将规格字符串转化为 BigDecimal 集合。
     *
     * @param specification 规格字符串
     * @return BigDecimal 集合
     */
    public static List<BigDecimal> specStrToDecList(String specification) {
        return Arrays.stream(specification.split(SPEC_SEP))
                .map(BigDecimal::new)
                .collect(Collectors.toList());
    }

    /**
     * 判断规格字符串是否为一个有效规格字符串，即厚度、宽度、长度中不存在为零的度量。
     *
     * @param specification 规格字符串
     * @return true 表示规格字符串有效，否则表示无效
     */
    public static boolean isValidSpec(String specification) {
        for (BigDecimal dec : specStrToDecList(specification)) {
            if (dec.compareTo(BigDecimal.ZERO) == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将规格数组转化为标准规格字符串。
     *
     * @param measures 规格数组
     * @return 标准规格字符串
     */
    public static String getStandardSpecStr(BigDecimal... measures) {
        return String.join(SPEC_SEP, Arrays.stream(measures)
                .map(measure -> measure.setScale(DEC_SCALE, RoundingMode.DOWN).toString())
                .toArray(String[]::new));
    }

    /**
     * 将规格字符串转化为标准格式的规格字符串。
     *
     * @param spec 规格字符串
     * @return 标准格式的规格字符串
     */
    public static String getStandardSpecStr(String spec) {
        return getStandardSpecStr(specStrToDecList(spec).toArray(BigDecimal[]::new));
    }

    /**
     * 依次比较两个规格字符串中的度量大小。
     *
     * @param sp1 规格1
     * @param sp2 规格2
     * @return -1，1分别表示前者规格中的某个维度度量小于、大于后者规格中的对应维度度量，0表示两者规格相同
     */
    public static int compareTwoSpecStr(String sp1, String sp2) {
        List<BigDecimal> decList1 = specStrToDecList(sp1);
        List<BigDecimal> decList2 = specStrToDecList(sp2);
        for (int i = 0; i < decList1.size(); i++) {
            if (decList1.get(i).compareTo(decList2.get(i)) != 0) {
                return decList1.get(i).compareTo(decList2.get(i));
            }
        }
        return 0;
    }

    public static int compareSpec(String sp1, String sp2) {
        List<BigDecimal> decList1 = specStrToDecList(sp1);
        List<BigDecimal> decList2 = specStrToDecList(sp2);
        if (decList1.get(0).compareTo(decList2.get(0)) != 0) {
            return decList2.get(0).compareTo(decList1.get(0));
        } else if (decList1.get(2).compareTo(decList2.get(2)) != 0) {
            return decList2.get(2).compareTo(decList1.get(2));
        } else if (decList1.get(1).compareTo(decList2.get(1)) != 0) {
            return decList1.get(1).compareTo(decList2.get(1));
        } else {
            return 0;
        }
    }

    /**
     * 比较第一个字符串所表示的规格是否大于等于第二个字符串所表示的规格。
     *
     * @param firstSpec  规格字符串一
     * @param secondSpec 规格字符串二
     * @return 结果
     */
    public static boolean isFirstSpecGeSecondSpec(String firstSpec, String secondSpec) {
        List<BigDecimal> decList1 = specStrToDecList(firstSpec);
        List<BigDecimal> decList2 = specStrToDecList(secondSpec);
        BigDecimal cuttingWidth = decList1.get(1);
        BigDecimal cuttingLength = decList1.get(2);
        BigDecimal productWidth = decList2.get(1);
        BigDecimal productLength = decList2.get(2);
        if (DecimalUtil.cmp(cuttingWidth, productWidth) >= 0 && DecimalUtil.cmp(cuttingLength, productLength) >= 0) {
            return true;
        } else {
            return DecimalUtil.cmp(cuttingWidth, productLength) >= 0 && DecimalUtil.cmp(cuttingLength, productWidth) >= 0;
        }
    }

    /**
     * 计算板材类型是属于余料还是废料，本质上每种板材类型都可以归结为这两个类别。
     *
     * @param boardWidth     板材宽度
     * @param boardLength    板材长度
     * @param wasteThreshold 废料阈值
     * @return 板材类型
     */
    public static BoardCategory calBoardCategory(BigDecimal boardWidth, BigDecimal boardLength, BigDecimal wasteThreshold) {
        return boardWidth.compareTo(wasteThreshold) >= 0 && boardLength.compareTo(wasteThreshold) >= 0 ? BoardCategory.REMAINING : BoardCategory.WASTE;
    }

    /**
     * 当后续裁剪板材为成品时，如果成品总宽度小于夹钳宽度且需修剪长度，则必须保证留有夹钳宽度大小的剩余宽度供裁剪成品。
     *
     * @param product   成品对象
     * @param preLength 预先板材长度
     * @return 保证成品板裁剪的总宽度
     */
    public static BigDecimal getPostProductAllWidth(NormalBoard product, BigDecimal preLength) {
        BigDecimal allWidth = product.getAllWidth();
        // 如果成品个数是在扣除夹钳深度后得到的个数，那么计算总宽度时需要加上夹钳深度，可以理解为原料板最后有夹钳深度的板材无法使用，在其之前的就是成品板材。
        allWidth = product.getWidth().compareTo(CLAMP_DEPTH) >= 0 ? allWidth : allWidth.add(CLAMP_DEPTH);
        if (allWidth.compareTo(CLAMP_WIDTH) < 0 && product.getLength().compareTo(preLength) < 0) {
            return CLAMP_WIDTH;
        }
        return allWidth;
    }

    /**
     * 当后续裁剪板材为非成品时，如果下料剩余宽度小于夹钳宽度且后续板材长度等于成品长度，则允许裁剪。
     *
     * @param remainingWidth 剩余宽度
     * @param productLength  成品长度
     * @param postLength     后续板材长度
     * @return 是否允许裁剪后续板材
     */
    public static boolean isAllowCutting(BigDecimal remainingWidth, BigDecimal productLength, BigDecimal postLength) {
        if (remainingWidth.compareTo(CLAMP_WIDTH) < 0 && productLength.compareTo(postLength) == 0) {
            return true;
        }
        return remainingWidth.compareTo(CLAMP_WIDTH) >= 0 && productLength.compareTo(postLength) >= 0;
    }

    /**
     * 获取原料板可用宽度，所谓可用宽度是指给定原料板和加工板的宽度，是否需要扣除夹钳深度。
     *
     * @param cutBoardWidth    原料板宽度
     * @param normalBoardWidth 加工板宽度
     * @return 原料板可用宽度
     */
    public static BigDecimal getAvailableWidth(BigDecimal cutBoardWidth, BigDecimal normalBoardWidth) {
        // 如果加工板宽度大于夹钳深度，那么整个原料板宽度都可以被用来裁剪加工板，如果余料小于夹钳深度，那就先把余料出掉；
        // 否则，原料板可用宽度必须扣除夹钳深度，防止夹钳深度不够余料或板材本身的裁剪。
        return DecimalUtil.cmp(normalBoardWidth, CLAMP_DEPTH) >= 0 ? cutBoardWidth : cutBoardWidth.subtract(CLAMP_DEPTH);
    }

    /**
     * 是否允许从后向前紧挨着排板裁剪，这取决于最后一类板材的宽度及其所需总宽度。
     *
     * @param normalBoard 板材对象
     * @return 是否允许从后向前紧挨着排板裁剪
     */
    public static boolean isAllowBackToFront(NormalBoard normalBoard) {
        // 如果说最后一类板材宽度总宽大于宽度阈值并且宽度大于深度阈值，那就允许从后向前紧挨着排板，因为既不需要预留空间也不用担心深度问题。
        // 如果只是前者不满足，那为了充分利用板材，会在中间将原本为了保证修边而补齐的原料给出去，然后推送最后一块板材。
        // 如果只是后者不满足，那就按照从前向后紧挨着的方式进行排板。
        return normalBoard.getAllWidth().compareTo(CLAMP_WIDTH) >= 0 && normalBoard.getWidth().compareTo(CLAMP_DEPTH) >= 0;
    }

    /**
     * 获取下料板。
     *
     * @param cuttingSize 规格
     * @param material    材质
     * @param forwardEdge 朝向
     * @return 下料板
     */
    public static CutBoard getCutBoard(String cuttingSize, String material, Integer forwardEdge, Integer orderId) {
        return new CutBoard(cuttingSize, material, forwardEdge == 1 ? ForwardEdge.LONG : ForwardEdge.SHORT, orderId);
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
    public static NormalBoard getStandardProduct(String specification, String material, BigDecimal cutBoardWidth, Integer orderIncompleteQuantity, Integer orderId) {
        NormalBoard product = new NormalBoard(specification, material, BoardCategory.PRODUCT, orderId);
        if (product.getWidth().compareTo(cutBoardWidth) > 0) {
            // 如果成品板宽度大于下料板宽度，则需要交换成品板的宽度和长度，不然会导致后续裁剪逻辑出错:
            BigDecimal tmp = product.getWidth();
            product.setWidth(product.getLength());
            product.setLength(tmp);
        }
        // 成品板的裁剪次数取决于最大裁剪次数以及工单未完成数目中的最小值:
        product.setCutTimes(Math.min(DecimalUtil.div(getAvailableWidth(cutBoardWidth, product.getWidth()), product.getWidth()), orderIncompleteQuantity));
        return product;
    }

    public static NormalBoard getStandardProduct(String specification, String material, Integer orderIncompleteQuantity, Integer orderId) {
        NormalBoard product = new NormalBoard(specification, material, BoardCategory.PRODUCT, orderId);
        if (product.getWidth().compareTo(product.getLength()) > 0) {
            // 如果成品板宽度大于下料板宽度，则需要交换成品板的宽度和长度，不然会导致后续裁剪逻辑出错:
            BigDecimal tmp = product.getWidth();
            product.setWidth(product.getLength());
            product.setLength(tmp);
        }
        // 成品板的裁剪次数取决于最大裁剪次数以及工单未完成数目中的最小值:
        product.setCutTimes(orderIncompleteQuantity);
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
    public static NormalBoard getSemiProduct(CutBoard cutBoard, BigDecimal fixedWidth, NormalBoard product) {
        NormalBoard semiProduct = new NormalBoard(cutBoard.getHeight(), fixedWidth, cutBoard.getLength(), cutBoard.getMaterial(), BoardCategory.SEMI_PRODUCT, cutBoard.getOrderId());
        if (DecimalUtil.cmp(semiProduct.getWidth(), BigDecimal.ZERO) > 0) {
            BigDecimal remainingWidth = DecimalUtil.sub(cutBoard.getWidth(), getPostProductAllWidth(product, semiProduct.getLength()));
            // 半成品的裁剪次数取决于下料板裁剪成品后的剩余宽度以及半成品自身宽度:
            semiProduct.setCutTimes(DecimalUtil.div(remainingWidth, semiProduct.getWidth()));
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
    public static NormalBoard getMatchStock(List<StockSpecification> specs, CutBoard cutBoard, NormalBoard product) {
        StockSpecification ss = specs.stream()
                .filter(spec -> spec.getHeight().compareTo(cutBoard.getHeight()) == 0)
                .findFirst()
                .orElse(ParamUtil.getDefaultStockSpec());
        NormalBoard stock = new NormalBoard(ss.getHeight(), ss.getWidth(), ss.getLength(), cutBoard.getMaterial(), BoardCategory.STOCK, cutBoard.getOrderId());
        if (stock.getWidth().compareTo(BigDecimal.ZERO) > 0 && cutBoard.getLength().compareTo(stock.getLength()) > 0) {
            if (product.getLength().compareTo(stock.getLength()) >= 0) {
                BigDecimal productAllWidth = product.getAllWidth();
                BigDecimal remainingWidth = DecimalUtil.sub(cutBoard.getWidth(), productAllWidth);
                if (isAllowCutting(remainingWidth, product.getLength(), stock.getLength())) {
                    stock.setCutTimes(DecimalUtil.div(getAvailableWidth(remainingWidth, stock.getWidth()), stock.getWidth()));
                }
            } else {
                BigDecimal productAllWidth = getPostProductAllWidth(product, stock.getLength());
                BigDecimal remainingWidth = DecimalUtil.sub(cutBoard.getWidth(), productAllWidth);
                stock.setCutTimes(DecimalUtil.div(remainingWidth, stock.getWidth()));
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
    public static NormalBoard getExtraBoard(CutBoard cutBoard, ForwardEdge forwardEdge, BigDecimal targetMeasure, BigDecimal wasteThreshold) {
        NormalBoard extraBoard = new NormalBoard();
        extraBoard.setHeight(cutBoard.getHeight());
        // 以进刀出去的边作为较长边:
        if (forwardEdge == ForwardEdge.LONG) {
            extraBoard.setLength(cutBoard.getLength());
            extraBoard.setWidth(DecimalUtil.sub(cutBoard.getWidth(), targetMeasure));
        } else {
            extraBoard.setLength(cutBoard.getWidth());
            extraBoard.setWidth(DecimalUtil.sub(cutBoard.getLength(), targetMeasure));
        }
        extraBoard.setMaterial(cutBoard.getMaterial());
        extraBoard.setCategory(calBoardCategory(extraBoard.getWidth(), extraBoard.getLength(), wasteThreshold));
        extraBoard.setOrderId(cutBoard.getOrderId());
        // 额外板材裁剪次数取决于目标度量和下料板对应度量的差值:
        extraBoard.setCutTimes(extraBoard.getWidth().compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
        return extraBoard;
    }

    /**
     * 获取后续成品。
     *
     * @param nextOrder 后续工单
     * @param cutBoard  原料板
     * @param product   当前工单成品板
     * @return 后续成品
     */
    public static NormalBoard getNextProduct(WorkOrder currOrder, WorkOrder nextOrder, CutBoard cutBoard, NormalBoard product) {
        NormalBoard nextProduct = new NormalBoard(nextOrder.getProductSpecification(), nextOrder.getMaterial(), BoardCategory.PRODUCT, nextOrder.getId());
        if (product.getMaterial().equals(nextProduct.getMaterial()) && currOrder.getBatchNumber().equals(nextOrder.getBatchNumber())) {
            BigDecimal remainingWidth = DecimalUtil.sub(cutBoard.getWidth(), product.getAllWidth());
            if (isAllowCutting(remainingWidth, product.getLength(), nextProduct.getLength())) {
                nextProduct.setCutTimes(Math.min(DecimalUtil.div(getAvailableWidth(remainingWidth, nextProduct.getWidth()), nextProduct.getWidth()), nextOrder.getIncompleteQuantity()));
            }
        }
        return nextProduct;
    }

    /**
     * 将下料信号中原料尺寸所包含的宽度度量减去较长边修边值。
     *
     * @param signal 下料信号对象
     */
    public static CuttingSignal changeCuttingSize(CuttingSignal signal) {
        List<BigDecimal> decList = specStrToDecList(signal.getCuttingSize());
        decList.set(1, decList.get(1).subtract(signal.getLongEdgeTrim()));
        CuttingSignal cuttingSignal = new CuttingSignal();
        cuttingSignal.setCuttingSize(getStandardSpecStr(decList.toArray(new BigDecimal[]{})));
        cuttingSignal.setForwardEdge(signal.getForwardEdge());
        cuttingSignal.setLongEdgeTrim(signal.getLongEdgeTrim());
        cuttingSignal.setOrderId(signal.getOrderId());
        return cuttingSignal;
    }
}
