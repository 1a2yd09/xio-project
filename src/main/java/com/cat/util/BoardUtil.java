package com.cat.util;

import com.cat.entity.Board;
import com.cat.entity.enums.BoardCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class BoardUtil {
    public static final String SPEC_SEP = "×";
    public static final int DEC_SCALE = 2;

    private BoardUtil() {
    }

    public static List<BigDecimal> specStrToDecList(String specification) {
        String[] specifications = specification.split(SPEC_SEP);
        return List.of(new BigDecimal(specifications[0]),
                new BigDecimal(specifications[1]),
                new BigDecimal(specifications[2]));
    }

    /**
     * 该方法仅用于在排序当中比较两个成品规格字符串，它一次只考虑规格中的一种度量，比如前者高度高，则前者排在前，不再考虑后续的宽度和长度度量。
     */
    public static int sortTwoSpecStr(String sp1, String sp2) {
        List<BigDecimal> decList1 = BoardUtil.specStrToDecList(sp1);
        List<BigDecimal> decList2 = BoardUtil.specStrToDecList(sp2);
        if (decList1.get(0).compareTo(decList2.get(0)) != 0) {
            return decList2.get(0).compareTo(decList1.get(0));
        } else if (decList1.get(1).compareTo(decList2.get(1)) != 0) {
            return decList2.get(1).compareTo(decList1.get(1));
        } else {
            return decList2.get(2).compareTo(decList1.get(2));
        }
    }

    public static String getStandardSpecStr(BigDecimal height, BigDecimal width, BigDecimal length) {
        // 同步数据表设计时设定的小数位数，规范输出格式:
        String heightStr = height.setScale(DEC_SCALE, RoundingMode.DOWN).toString();
        String widthStr = width.setScale(DEC_SCALE, RoundingMode.DOWN).toString();
        String lengthStr = length.setScale(DEC_SCALE, RoundingMode.DOWN).toString();
        return String.join(SPEC_SEP, heightStr, widthStr, lengthStr);
    }

    public static String getStandardSpecStr(String spec) {
        List<BigDecimal> list = specStrToDecList(spec);
        return getStandardSpecStr(list.get(0), list.get(1), list.get(2));
    }

    /**
     * 虽取名叫计算板材类型，但实际是计算板材是属于余料或者废料类型。
     */
    public static BoardCategory calBoardCategory(BigDecimal boardWidth, BigDecimal boardLength, BigDecimal wasteThreshold) {
        if (boardWidth.compareTo(wasteThreshold) >= 0 && boardLength.compareTo(wasteThreshold) >= 0) {
            return BoardCategory.REMAINING;
        } else {
            return BoardCategory.WASTED;
        }
    }
}
