package com.cat.entity;

import com.cat.entity.enums.BoardCategory;

import java.math.BigDecimal;
import java.util.Objects;

public class CutBoard extends Board {
    public enum EdgeType {
        /**
         * 短边
         */
        SHORT,
        /**
         * 长边
         */
        LONG;
    }

    /**
     * 0表示短边朝前，1表示长边朝前。
     */
    private EdgeType forwardEdge;

    public CutBoard(BigDecimal height, BigDecimal width, BigDecimal length, String material) {
        super(height, width, length, material, BoardCategory.CUTTING);
        this.forwardEdge = EdgeType.SHORT;
    }

    public CutBoard(String specification, String material) {
        super(specification, material, BoardCategory.CUTTING);
        this.forwardEdge = EdgeType.SHORT;
    }

    public void changeForwardEdge() {
        if (this.forwardEdge == EdgeType.LONG) {
            this.forwardEdge = EdgeType.SHORT;
        } else {
            this.forwardEdge = EdgeType.LONG;
        }
    }

    public void modifySpec(BigDecimal val) {
        if (this.forwardEdge == EdgeType.LONG) {
            this.setWidth(this.getWidth().subtract(val));
        } else {
            this.setLength(this.getLength().subtract(val));
        }
    }

    public EdgeType getForwardEdge() {
        return forwardEdge;
    }

    public void setForwardEdge(EdgeType forwardEdge) {
        this.forwardEdge = forwardEdge;
    }

    @Override
    public String toString() {
        return super.toString() + " CutBoard{" +
                "forwardEdge=" + forwardEdge +
                '}';
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(this.forwardEdge);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CutBoard) {
            CutBoard cb = (CutBoard) obj;
            return super.equals(obj) && Objects.equals(this.forwardEdge, cb.forwardEdge);
        }
        return false;
    }
}
