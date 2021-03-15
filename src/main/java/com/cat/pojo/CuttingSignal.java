package com.cat.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author CAT
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CuttingSignal extends BaseSignal {
    private String cuttingSize;
    private Integer forwardEdge;
    private Integer orderId;

    public CuttingSignal(String cuttingSize, Integer forwardEdge, Integer orderId) {
        this.cuttingSize = cuttingSize;
        this.forwardEdge = forwardEdge;
        this.orderId = orderId;
    }
}
