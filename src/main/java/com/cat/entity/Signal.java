package com.cat.entity;

import java.time.LocalDateTime;

public class Signal {
    private Integer id;
    private String category;
    private Boolean processed;
    private LocalDateTime createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Signal{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", processed=" + processed +
                ", createdAt=" + createdAt +
                '}';
    }
}
