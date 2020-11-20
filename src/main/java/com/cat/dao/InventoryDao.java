package com.cat.dao;

import com.cat.entity.Inventory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryDao extends BaseDao {
    RowMapper<Inventory> inventoryM = new BeanPropertyRowMapper<>(Inventory.class);

    public List<Inventory> getInventories(String material, String category) {
        return this.jdbcTemplate.query("SELECT * FROM tb_inventory WHERE material = ? AND category = ?", this.inventoryM, material, category);
    }

    public List<Inventory> getInventories(String category) {
        return this.jdbcTemplate.query("SELECT * FROM tb_inventory WHERE category = ?", this.inventoryM, category);
    }

    public void insertInventory(String specification, String material, Integer amount, String category) {
        this.jdbcTemplate.update("INSERT INTO tb_inventory(specification, material, amount, category) " +
                "VALUES (?, ?, ?, ?)", specification, material, amount, category);
    }

    public void updateInventoryAmount(Integer amount, Long id) {
        this.jdbcTemplate.update("UPDATE tb_inventory SET amount = ? WHERE id = ?", amount, id);
    }

    public void truncateTable() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_inventory");
    }
}
