package com.cat.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BoardList {
    private List<Board> boards;
    private BigDecimal boardAllWidth;

    public BoardList() {
        this.boards = new ArrayList<>();
        this.boardAllWidth = BigDecimal.ZERO;
    }

    public List<Board> getBoards() {
        return boards;
    }

    public BigDecimal getBoardAllWidth() {
        return boardAllWidth;
    }

    public void addBoard(Board board) {
        this.boards.add(board);
        this.boardAllWidth = this.boardAllWidth.add(board.getNormalBoard().getNormalBoardAllWidth());
    }
}
