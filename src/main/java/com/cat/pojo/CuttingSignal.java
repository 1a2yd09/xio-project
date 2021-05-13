package com.cat.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

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
    private BigDecimal longEdgeTrim;
    private Integer orderId;

    public CuttingSignal(String cuttingSize, Integer forwardEdge, BigDecimal longEdgeTrim, Integer orderId) {
        this.cuttingSize = cuttingSize;
        this.forwardEdge = forwardEdge;
        this.longEdgeTrim = longEdgeTrim;
        this.orderId = orderId;
    }
}
