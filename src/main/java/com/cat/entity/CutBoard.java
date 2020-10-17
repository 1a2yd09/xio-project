package com.cat.entity;

import com.cat.entity.enums.BoardCategory;

public class CutBoard extends Board {
    /**
     * 0表示短边朝前，1表示长边朝前。
     */
    private Integer forwardEdge;

    public CutBoard(String specification, String material, BoardCategory category, Integer forwardEdge) {
        super(specification, material, category);
        this.forwardEdge = forwardEdge;
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
