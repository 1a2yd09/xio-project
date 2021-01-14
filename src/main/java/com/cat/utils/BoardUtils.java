package com.cat.utils;

import com.cat.entity.board.NormalBoard;
import com.cat.enums.BoardCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author CAT
 */
public class BoardUtils {
    /**
     * 夹钳宽度
     */
    public static final BigDecimal CLAMP_WIDTH = new BigDecimal(900);
    /**
     * 夹钳深度
     */
    public static final BigDecimal CLAMP_DEPTH = new BigDecimal(50);
    /**
     * 规格字符串分隔符
     */
    public static final String SPEC_SEP = "×";
    /**
     * 规格度量精度
     */
    public static final int DEC_SCALE = 2;

    private BoardUtils() {
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
     * @return -1，1分别表示前者规格中的某个度量小于、大于后者规格中的对应度量，0表示两者规格相同
     */
    public static int compareTwoSpecStr(String sp1, String sp2) {
        List<BigDecimal> decList1 = BoardUtils.specStrToDecList(sp1);
        List<BigDecimal> decList2 = BoardUtils.specStrToDecList(sp2);
        for (int i = 0; i < decList1.size(); i++) {
            if (decList1.get(i).compareTo(decList2.get(i)) != 0) {
                return decList1.get(i).compareTo(decList2.get(i));
            }
        }
        return 0;
    }

    /**
     * 比较第一个字符串所表示的规格是否大于等于第二个字符串所表示的规格。
     *
     * @param firstSpec  规格字符串一
     * @param secondSpec 规格字符串二
     * @return 结果
     */
    public static boolean isFirstSpecGeSecondSpec(String firstSpec, String secondSpec) {
        List<BigDecimal> decList1 = BoardUtils.specStrToDecList(firstSpec);
        List<BigDecimal> decList2 = BoardUtils.specStrToDecList(secondSpec);
        BigDecimal cuttingWidth = decList1.get(1);
        BigDecimal cuttingLength = decList1.get(2);
        BigDecimal productWidth = decList2.get(1);
        BigDecimal productLength = decList2.get(2);
        if (Arith.cmp(cuttingWidth, productWidth) >= 0 && Arith.cmp(cuttingLength, productLength) >= 0) {
            return true;
        } else {
            return Arith.cmp(cuttingWidth, productLength) >= 0 && Arith.cmp(cuttingLength, productWidth) >= 0;
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

    public static Queue<NormalBoard> getBoardLengthPriorityQueue() {
        return new PriorityQueue<>(Comparator.comparing(NormalBoard::getLength).reversed());
    }

    /**
     * 当后续裁剪板材为成品时，如果成品总宽度小于夹钳宽度且需修剪长度，则必须保证留有夹钳宽度大小的剩余宽度供裁剪成品。
     *
     * @param productAllWidth 成品所需总宽度
     * @param productLength   成品长度
     * @param preLength       预先板材长度
     * @return 保证成品板裁剪的总宽度
     */
    public static BigDecimal processPostProductAllWidth(BigDecimal productAllWidth, BigDecimal productLength, BigDecimal preLength) {
        if (productAllWidth.compareTo(CLAMP_WIDTH) < 0 && productLength.compareTo(preLength) < 0) {
            return CLAMP_WIDTH;
        }
        return productAllWidth;
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
}
