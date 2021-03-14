package com.cat.entity.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author CAT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockSpecification {
    private Long id;
    private BigDecimal height;
    private BigDecimal width;
    private BigDecimal length;
    private LocalDateTime createdAt;
}
