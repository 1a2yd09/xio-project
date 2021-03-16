package com.cat.pojo;

import com.cat.enums.BoardCategory;
import com.cat.enums.ForwardEdge;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author CAT
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CutBoard extends BaseBoard {
    private ForwardEdge forwardEdge;


    public CutBoard(String specification, String material, Integer orderId) {
        super(specification, material, BoardCategory.CUTTING, orderId);
        this.forwardEdge = ForwardEdge.SHORT;
    }

    public CutBoard(String specification, String material, Integer orderId, ForwardEdge forwardEdge) {
        super(specification, material, BoardCategory.CUTTING, orderId);
        this.forwardEdge = forwardEdge;
    }
}
