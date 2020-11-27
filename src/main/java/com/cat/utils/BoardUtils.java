package com.cat.utils;

import com.cat.enums.BoardCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BoardUtils {
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
}
