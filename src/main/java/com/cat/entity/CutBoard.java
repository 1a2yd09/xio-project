package com.cat.entity;

import com.cat.entity.enums.BoardCategory;

public class CutBoard extends BaseBoard {
    public enum EdgeType {
        /**
         * 短边。
         */
        SHORT,
        /**
         * 长边。
         */
        LONG
    }

    private EdgeType forwardEdge;

    public CutBoard(String specification, String material, EdgeType forwardEdge) {
        super(specification, material, BoardCategory.CUTTING);
        this.forwardEdge = forwardEdge;
    }

    public EdgeType getForwardEdge() {
        return forwardEdge;
    }

    public void setForwardEdge(EdgeType forwardEdge) {
        this.forwardEdge = forwardEdge;
    }

    @Override
    public String toString() {
        return super.toString() +
                "CutBoard{" +
                "forwardEdge=" + forwardEdge +
                '}';
    }
}
