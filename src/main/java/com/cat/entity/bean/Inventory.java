package com.cat.entity.bean;

import java.time.LocalDateTime;

/**
 * @author CAT
 */
public class Inventory {
    private Long id;
    private String specification;
    private String material;
    private Integer quantity;
    private String category;
    private LocalDateTime createdAt;

    public Inventory() {
    }

    public Inventory(String specification, String material, String category) {
        this.specification = specification;
        this.material = material;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "id=" + id +
                ", specification='" + specification + '\'' +
                ", material='" + material + '\'' +
                ", quantity=" + quantity +
                ", category='" + category + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}