package com.cat.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TrimmingValue {
    private Integer id;
    private BigDecimal trimTop;
    private BigDecimal trimLeft;
    private BigDecimal trimBottom;
    private BigDecimal trimRight;
    private LocalDateTime createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getTrimTop() {
        return trimTop;
    }

    public void setTrimTop(BigDecimal trimTop) {
        this.trimTop = trimTop;
    }

    public BigDecimal getTrimLeft() {
        return trimLeft;
    }

    public void setTrimLeft(BigDecimal trimLeft) {
        this.trimLeft = trimLeft;
    }

    public BigDecimal getTrimBottom() {
        return trimBottom;
    }

    public void setTrimBottom(BigDecimal trimBottom) {
        this.trimBottom = trimBottom;
    }

    public BigDecimal getTrimRight() {
        return trimRight;
    }

    public void setTrimRight(BigDecimal trimRight) {
        this.trimRight = trimRight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "TrimmingParameter{" +
                "id=" + id +
                ", trimTop=" + trimTop +
                ", trimLeft=" + trimLeft +
                ", trimBottom=" + trimBottom +
                ", trimRight=" + trimRight +
                ", createdAt=" + createdAt +
                '}';
    }
}
