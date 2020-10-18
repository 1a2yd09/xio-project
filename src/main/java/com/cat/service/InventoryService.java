package com.cat.service;

import com.cat.entity.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<Inventory> inventoryM = new BeanPropertyRowMapper<>(Inventory.class);

    public void addInventory(String specification, String material, int amount, String category) {
        Inventory inventory = this.getInventory(specification, material, category);
        if (inventory != null) {
            // 如果存在对应的存货数据，将数量进行相加:
            inventory.setAmount(inventory.getAmount() + amount);
            this.updateInventoryAmount(inventory);
        } else {
            // 如果不存在对应的存货数据，插入一条新的存货数据，数量字段取值为1:
            this.addNewInventory(specification, material, 1, category);
        }
    }

    public void clearAllInventory() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_inventory");
    }

    public Integer getInventoryCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_inventory", Integer.class);
    }

    public Inventory getInventory(String specification, String material, String category) {
        // TODO: 虽然规格字符串在整个项目中都是按照确定格式输出的，但是直接比较规格字符串感觉还是有点不稳妥。
        List<Inventory> list = this.jdbcTemplate.query("SELECT * FROM tb_inventory WHERE specification = ? AND material = ? AND category = ?", this.inventoryM, specification, material, category);
        return list.isEmpty() ? null : list.get(0);
    }

    public void addNewInventory(String specification, String material, int amount, String category) {
        this.jdbcTemplate.update("INSERT INTO tb_inventory (specification, material, amount, category) VALUES (?, ?, ?, ?)", specification, material, amount, category);
    }

    public void updateInventoryAmount(Inventory inventory) {
        this.jdbcTemplate.update("UPDATE tb_inventory SET amount = ? WHERE id = ?", inventory.getAmount(), inventory.getId());
    }
}
