package com.cat.entity;

import com.cat.entity.enums.BoardCategory;

import java.math.BigDecimal;

public class CutBoard extends Board {
    /**
     * 0表示短边朝前，1表示长边朝前。
     */
    private Integer forwardEdge;

    public CutBoard(BigDecimal height, BigDecimal width, BigDecimal length, String material, BoardCategory category) {
        super(height, width, length, material, category);
        this.forwardEdge = 0;
    }

    public CutBoard(String specification, String material, BoardCategory category) {
        super(specification, material, category);
        this.forwardEdge = 0;
    }

    public Integer getForwardEdge() {
        return forwardEdge;
    }

    public void setForwardEdge(Integer forwardEdge) {
        this.forwardEdge = forwardEdge;
    }

    @Override
    public String toString() {
        return super.toString() + "CutBoard{" +
                "forwardEdge=" + forwardEdge +
                '}';
    }
}
