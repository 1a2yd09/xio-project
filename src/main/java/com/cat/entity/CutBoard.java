package com.cat.entity;

import com.cat.entity.enums.BoardCategory;

import java.math.BigDecimal;

public class CutBoard extends Board {
    /**
     * 0表示短边朝前，1表示长边朝前。
     */
    private Integer forwardEdge;

    public CutBoard(BigDecimal height, BigDecimal width, BigDecimal length, String material) {
        super(height, width, length, material, BoardCategory.CUTTING);
        this.forwardEdge = 0;
    }

    public CutBoard(String specification, String material) {
        super(specification, material, BoardCategory.CUTTING);
        this.forwardEdge = 0;
    }

    public void changeForwardEdge() {
        if (this.forwardEdge == 1) {
            this.forwardEdge = 0;
        } else {
            this.forwardEdge = 1;
        }
    }

    public void modifySpec(BigDecimal val) {
        if (this.forwardEdge == 1) {
            this.setWidth(this.getWidth().subtract(val));
        } else {
            this.setLength(this.getLength().subtract(val));
        }
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
