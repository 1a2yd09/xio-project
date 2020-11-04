package com.cat.entity;

import com.cat.entity.enums.BoardCategoryEnum;

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

    public CutBoard(String specification, String material) {
        super(specification, material, BoardCategoryEnum.CUTTING);
        this.forwardEdge = EdgeType.SHORT;
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
