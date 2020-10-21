package com.cat.util;

import com.cat.entity.Board;
import com.cat.entity.enums.BoardCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class BoardUtil {
    public static final String SPEC_SEP = "×";
    public static final int DEC_SCALE = 2;

    public static List<BigDecimal> specStrToDecList(String specification) {
        String[] specifications = specification.split(SPEC_SEP);
        return List.of(new BigDecimal(specifications[0]),
                new BigDecimal(specifications[1]),
                new BigDecimal(specifications[2]));
    }

    /**
     * 该方法仅用于在排序当中比较两个规格字符串，它一次只考虑规格中的一种度量，
     * 比如前者高度高，则前者排在前，不考虑后续的宽度和长度条件。
     */
    public static int sortTwoSpecStr(String sp1, String sp2) {
        // TODO: 这里排序可能要考虑宽度和长度的大小关系。
        List<BigDecimal> o1List = BoardUtil.specStrToDecList(sp1);
        List<BigDecimal> o2List = BoardUtil.specStrToDecList(sp2);
        if (o1List.get(0).compareTo(o2List.get(0)) != 0) {
            return o2List.get(0).compareTo(o1List.get(0));
        } else if (o1List.get(1).compareTo(o2List.get(1)) != 0) {
            return o2List.get(1).compareTo(o1List.get(1));
        } else {
            return o2List.get(2).compareTo(o1List.get(2));
        }
    }

    public static String getStandardSpecStr(BigDecimal height, BigDecimal width, BigDecimal length) {
        // 同步数据表设计时设定的小数位数，规范化输出格式:
        String heightStr = height.setScale(DEC_SCALE, RoundingMode.DOWN).toString();
        String widthStr = width.setScale(DEC_SCALE, RoundingMode.DOWN).toString();
        String lengthStr = length.setScale(DEC_SCALE, RoundingMode.DOWN).toString();
        return String.join(SPEC_SEP, heightStr, widthStr, lengthStr);
    }

    public static String getStandardSpecStr(String spec) {
        List<BigDecimal> list = specStrToDecList(spec);
        return getStandardSpecStr(list.get(0), list.get(1), list.get(2));
    }

    public static BoardCategory calBoardCategory(BigDecimal boardWidth, BigDecimal boardLength, BigDecimal wasteThreshold) {
        if (boardWidth.compareTo(wasteThreshold) >= 0 && boardLength.compareTo(wasteThreshold) >= 0) {
            return BoardCategory.REMAINING;
        } else {
            return BoardCategory.WASTED;
        }
    }

    public static void standardizingBoard(Board board) {
        if (board.getWidth().compareTo(board.getLength()) > 0) {
            BigDecimal tmp = board.getWidth();
            board.setWidth(board.getLength());
            board.setLength(tmp);
        }
    }

    public static Board getStandardBoard(String specification, String material, BoardCategory category) {
        Board board = new Board(specification, material, category);
        BoardUtil.standardizingBoard(board);
        return board;
    }
}
