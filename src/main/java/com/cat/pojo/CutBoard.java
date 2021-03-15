package com.cat.pojo;

import com.cat.enums.BoardCategory;
import com.cat.enums.ForwardEdge;
import lombok.*;

/**
 * @author CAT
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
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
}
