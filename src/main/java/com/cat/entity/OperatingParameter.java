package com.cat.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OperatingParameter {
    private Integer id;
    private LocalDate workOrderDate;
    private BigDecimal fixedWidth;
    private BigDecimal wasteThreshold;
    private String bottomOrderSort;
    private LocalDateTime createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getWorkOrderDate() {
        return workOrderDate;
    }

    public void setWorkOrderDate(LocalDate workOrderDate) {
        this.workOrderDate = workOrderDate;
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

    public String getBottomOrderSort() {
        return bottomOrderSort;
    }

    public void setBottomOrderSort(String bottomOrderSort) {
        this.bottomOrderSort = bottomOrderSort;
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
                ", workOrderDate=" + workOrderDate +
                ", fixedWidth=" + fixedWidth +
                ", wasteThreshold=" + wasteThreshold +
                ", bottomOrderSort='" + bottomOrderSort + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
