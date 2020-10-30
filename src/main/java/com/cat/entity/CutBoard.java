package com.cat.entity;

import com.cat.entity.enums.BoardCategory;

public class CutBoard extends AbstractBoard {
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

    public CutBoard(String specification, String material) {
        super(specification, material, BoardCategory.CUTTING);
        this.forwardEdge = EdgeType.SHORT;
    }

    public EdgeType getForwardEdge() {
        return forwardEdge;
    }

    public void setForwardEdge(EdgeType forwardEdge) {
        this.forwardEdge = forwardEdge;
    }
}
