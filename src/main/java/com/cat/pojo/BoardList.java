package com.cat.pojo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CAT
 */
public class BoardList {
    private final List<NormalBoard> boards;
    private BigDecimal boardAllWidth;

    public BoardList() {
        this.boards = new ArrayList<>();
        this.boardAllWidth = BigDecimal.ZERO;
    }

    public List<NormalBoard> getBoards() {
        return boards;
    }

    public BigDecimal getBoardAllWidth() {
        return boardAllWidth;
    }

    public NormalBoard getLastBoard() {
        return this.boards.get(this.boards.size() - 1);
    }

    /**
     * 添加板材对象至板材列表，裁剪次数为零的板材对象将被过滤。
     *
     * @param board 板材对象
     */
    public void addBoard(NormalBoard board) {
        if (board.getCutTimes() > 0) {
            this.boards.add(board);
            this.boardAllWidth = this.boardAllWidth.add(board.getAllWidth());
        }
    }
}
