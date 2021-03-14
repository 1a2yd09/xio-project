package com.cat.entity.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author CAT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    private Long id;
    private String specification;
    private String material;
    private Integer quantity;
    private String category;
    private LocalDateTime createdAt;

    public Inventory(String specification, String material, String category) {
        this.specification = specification;
        this.material = material;
        this.category = category;
    }
}
