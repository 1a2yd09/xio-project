package com.cat.entity;

import com.cat.entity.board.NormalBoard;

public class Board {
    private Integer orderId;
    private NormalBoard normalBoard;

    public Board(Integer orderId, NormalBoard normalBoard) {
        this.orderId = orderId;
        this.normalBoard = normalBoard;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public NormalBoard getNormalBoard() {
        return normalBoard;
    }

    public void setNormalBoard(NormalBoard normalBoard) {
        this.normalBoard = normalBoard;
    }
}
