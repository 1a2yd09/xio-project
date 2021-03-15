package com.cat.pojo;

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

    public Inventory(String category) {
        this.category = category;
    }

    public Inventory(String material, String category) {
        this.material = material;
        this.category = category;
    }

    public Inventory(String specification, String material, String category) {
        this.specification = specification;
        this.material = material;
        this.category = category;
    }

    public Inventory(String specification, String material, Integer quantity, String category) {
        this.specification = specification;
        this.material = material;
        this.quantity = quantity;
        this.category = category;
    }
}
