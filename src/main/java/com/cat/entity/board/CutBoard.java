package com.cat.entity.board;

import com.cat.enums.BoardCategory;
import com.cat.enums.ForwardEdge;

/**
 * @author CAT
 */
public class CutBoard extends BaseBoard {
    private ForwardEdge forwardEdge;

    public CutBoard(String specification, String material) {
        super(specification, material, BoardCategory.CUTTING);
        this.forwardEdge = ForwardEdge.SHORT;
    }

    public CutBoard(String specification, String material, ForwardEdge forwardEdge) {
        super(specification, material, BoardCategory.CUTTING);
        this.forwardEdge = forwardEdge;
    }

    public ForwardEdge getForwardEdge() {
        return forwardEdge;
    }

    public void setForwardEdge(ForwardEdge forwardEdge) {
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
