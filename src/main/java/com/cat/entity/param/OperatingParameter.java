package com.cat.entity.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author CAT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperatingParameter {
    private Long id;
    private LocalDate orderDate;
    private BigDecimal fixedWidth;
    private BigDecimal wasteThreshold;
    private String sortPattern;
    private String orderModule;
    private LocalDateTime createdAt;

    public OperatingParameter(LocalDate orderDate, BigDecimal fixedWidth, BigDecimal wasteThreshold, String sortPattern, String orderModule) {
        this.orderDate = orderDate;
        this.fixedWidth = fixedWidth;
        this.wasteThreshold = wasteThreshold;
        this.sortPattern = sortPattern;
        this.orderModule = orderModule;
    }
}
