package com.cat.entity.param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author CAT
 */
public class OperatingParameter {
    private Long id;
    private LocalDate orderDate;
    private BigDecimal fixedWidth;
    private BigDecimal wasteThreshold;
    private String sortPattern;
    private LocalDateTime createdAt;

    public OperatingParameter() {
    }

    public OperatingParameter(Long id, LocalDate orderDate, BigDecimal fixedWidth, BigDecimal wasteThreshold, String sortPattern, LocalDateTime createdAt) {
        this.id = id;
        this.orderDate = orderDate;
        this.fixedWidth = fixedWidth;
        this.wasteThreshold = wasteThreshold;
        this.sortPattern = sortPattern;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getFixedWidth() {
        return fixedWidth;
    }

    public void setFixedWidth(BigDecimal fixedWidth) {
        this.fixedWidth = fixedWidth;
    }

    public BigDecimal getWasteThreshold() {
        return wasteThreshold;
    }

    public void setWasteThreshold(BigDecimal wasteThreshold) {
        this.wasteThreshold = wasteThreshold;
    }

    public String getSortPattern() {
        return sortPattern;
    }

    public void setSortPattern(String sortPattern) {
        this.sortPattern = sortPattern;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "OperatingParameter{" +
                "id=" + id +
                ", orderDate=" + orderDate +
                ", fixedWidth=" + fixedWidth +
                ", wasteThreshold=" + wasteThreshold +
                ", sortPattern='" + sortPattern + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
