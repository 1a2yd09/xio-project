package com.cat.util;

import com.cat.entity.enums.BoardCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BoardUtils {
    public static final String SPEC_SEP = "×";
    public static final int DEC_SCALE = 2;

    private BoardUtils() {
    }

    public static List<BigDecimal> specStrToDecList(String specification) {
        return Arrays.stream(specification.split(SPEC_SEP))
                .map(BigDecimal::new)
                .collect(Collectors.toList());
    }

    public static String getStandardSpecStr(BigDecimal... measures) {
        return String.join(SPEC_SEP, Arrays.stream(measures)
                .map(measure -> measure.setScale(DEC_SCALE, RoundingMode.DOWN).toString())
                .toArray(String[]::new));
    }

    public static String getStandardSpecStr(String spec) {
        return getStandardSpecStr(specStrToDecList(spec).toArray(BigDecimal[]::new));
    }

    public static int compareTwoSpecStr(String sp1, String sp2) {
        List<BigDecimal> decList1 = BoardUtils.specStrToDecList(sp1);
        List<BigDecimal> decList2 = BoardUtils.specStrToDecList(sp2);
        for (int i = 0; i < decList1.size(); i++) {
            if (decList1.get(i).compareTo(decList2.get(i)) != 0) {
                return decList2.get(i).compareTo(decList1.get(i));
            }
        }
        return 0;
    }

    /**
     * 虽取名叫计算板材类型，但实际是计算板材的类型是属于余料还是废料，本质上每种板材类型都可以归结为这两个类别。
     */
    public static BoardCategory calBoardCategory(BigDecimal boardWidth, BigDecimal boardLength, BigDecimal wasteThreshold) {
        return boardWidth.compareTo(wasteThreshold) >= 0 && boardLength.compareTo(wasteThreshold) >= 0 ? BoardCategory.REMAINING : BoardCategory.WASTED;
    }
}
