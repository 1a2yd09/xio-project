package com.cat.entity.board;

import com.cat.enums.BoardCategory;

import java.math.BigDecimal;

public class NormalBoard extends BaseBoard {
    private Integer cutTimes;

    public NormalBoard() {
    }

    public NormalBoard(BigDecimal height, BigDecimal width, BigDecimal length, String material, BoardCategory category) {
        super(height, width, length, material, category);
        this.cutTimes = 0;
    }

    public NormalBoard(String specification, String material, BoardCategory category) {
        super(specification, material, category);
        this.cutTimes = 0;
    }

    public Integer getCutTimes() {
        return cutTimes;
    }

    public void setCutTimes(Integer cutTimes) {
        this.cutTimes = cutTimes;
    }

    @Override
    public String toString() {
        return super.toString() +
                "NormalBoard{" +
                "cutTimes=" + cutTimes +
                '}';
    }
}
