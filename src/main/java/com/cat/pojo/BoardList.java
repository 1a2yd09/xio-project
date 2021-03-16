package com.cat.pojo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 添加板材对象至板材列表。
     *
     * @param board 板材对象
     */
    public void addBoard(NormalBoard board) {
        this.boards.add(board);
        this.boardAllWidth = this.boardAllWidth.add(board.getNormalBoardAllWidth());
    }
}
