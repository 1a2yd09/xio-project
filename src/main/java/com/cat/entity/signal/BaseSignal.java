package com.cat.entity.signal;

import java.time.LocalDateTime;

/**
 * @author CAT
 */
public abstract class BaseSignal {
    private Long id;
    private Boolean processed;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return "BaseSignal{" +
                "id=" + id +
                ", processed=" + processed +
                ", createdAt=" + createdAt +
                '}';
    }
}
