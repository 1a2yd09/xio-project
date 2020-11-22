package com.cat.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockSpecification {
    private Long id;
    private BigDecimal height;
    private BigDecimal width;
    private BigDecimal length;
    private LocalDateTime createdAt;

    public StockSpecification() {
    }

    public StockSpecification(Long id, BigDecimal height, BigDecimal width, BigDecimal length, LocalDateTime createdAt) {
        this.id = id;
        this.height = height;
        this.width = width;
        this.length = length;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public BigDecimal getLength() {
        return length;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "StockSpecification{" +
                "id=" + id +
                ", height=" + height +
                ", width=" + width +
                ", length=" + length +
                ", createdAt=" + createdAt +
                '}';
    }
}
