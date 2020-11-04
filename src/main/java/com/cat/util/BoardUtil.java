package com.cat.util;

import com.cat.entity.enums.BoardCategoryEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BoardUtil {
    public static final String SPEC_SEP = "×";
    public static final int DEC_SCALE = 2;

    private BoardUtil() {
    }

    public static List<BigDecimal> specStrToDecList(String specification) {
        return Arrays.stream(specification.split(SPEC_SEP)).map(BigDecimal::new).collect(Collectors.toList());
    }

    public static String getStandardSpecStr(BigDecimal... measures) {
        return String.join(SPEC_SEP, Arrays.stream(measures).map(measure -> measure.setScale(DEC_SCALE, RoundingMode.DOWN).toString()).toArray(String[]::new));
    }

    public static String getStandardSpecStr(String spec) {
        return getStandardSpecStr(specStrToDecList(spec).toArray(BigDecimal[]::new));
    }

    /**
     * 该方法仅用于在排序当中比较两个成品规格字符串，它一次只考虑规格中的一种度量，比如前者高度高，则前者排在前，不再考虑宽度和长度。
     */
    public static int compareTwoSpecStr(String sp1, String sp2) {
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

    public static boolean isTwoSpecStrEqual(String spec1, String spec2) {
        List<BigDecimal> decList1 = BoardUtil.specStrToDecList(spec1);
        List<BigDecimal> decList2 = BoardUtil.specStrToDecList(spec2);
        for (int i = 0; i < decList1.size(); i++) {
            if (decList1.get(i).compareTo(decList2.get(i)) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 虽取名叫计算板材类型，但实际是计算板材的类型是属于余料还是废料。
     */
    public static BoardCategoryEnum calBoardCategory(BigDecimal boardWidth, BigDecimal boardLength, BigDecimal wasteThreshold) {
        return boardWidth.compareTo(wasteThreshold) >= 0 && boardLength.compareTo(wasteThreshold) >= 0 ? BoardCategoryEnum.REMAINING : BoardCategoryEnum.WASTED;
    }
}
