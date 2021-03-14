package com.cat.entity.signal;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author CAT
 */
@Data
public abstract class BaseSignal {
    private Long id;
    private Boolean processed;
    private LocalDateTime createdAt;
}
