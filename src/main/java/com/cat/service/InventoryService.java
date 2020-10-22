package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.Inventory;
import com.cat.entity.enums.BoardCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InventoryService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<Inventory> inventoryM = new BeanPropertyRowMapper<>(Inventory.class);

    public Map<String, Inventory> getStockMap() {
        List<Inventory> inventories = this.getInventories(BoardCategory.STOCK.value);
        Map<String, Inventory> map = new HashMap<>(inventories.size());
        for (Inventory inventory : inventories) {
            map.put(inventory.getSpecification(), inventory);
        }
        return map;
    }

    public void addInventory(Board board, int amount) {
        Inventory inventory = this.getInventory(board.getSpecification(), board.getMaterial(), board.getCategory().value);
        if (inventory != null) {
            // 如果存在对应的存货数据，将数量进行相加:
            inventory.setAmount(inventory.getAmount() + amount);
            this.updateInventoryAmount(inventory);
        } else {
            // 如果不存在对应的存货数据，插入一条新的存货数据，数量字段取值为传入的参数数量:
            // 2020/10/22 BUG: 之前都是一个存货就执行一次添加操作，于是这里 amount 字段直接就传入 1 了，但后续改成多次记录一次添加，这里要改成传入参数:
            // 之前的测试能通过是因为之前存货表里已经存入了相同数据，导致没有测试到该分支条件。
            this.addNewInventory(board.getSpecification(), board.getMaterial(), amount, board.getCategory().value);
        }
    }

    public void clearAllInventory() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_inventory");
    }

    public Integer getInventoryCount() {
        return this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_inventory", Integer.class);
    }

    public Inventory getInventory(String specification, String material, String category) {
        // 这里的规格字符串是 Board 对象调用 getSpecification() 传入的，虽然保证写入和获取时使用同一方法，但是直接比较字符串感觉还是有点不稳妥。
        // TODO: 首先根据类型、材质获取存货集合，定义一个用于比较两个规格字符串的方法，这样就不用借助 Board 对象的比较方法。
        List<Inventory> list = this.jdbcTemplate.query("SELECT * FROM tb_inventory WHERE specification = ? AND material = ? AND category = ?", this.inventoryM, specification, material, category);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Inventory> getInventories(String category) {
        // 这里只获取那些数量不为零的存货:
        return this.jdbcTemplate.query("SELECT * FROM tb_inventory WHERE amount > 0 AND category = ?", this.inventoryM, category);
    }

    public void addNewInventory(String specification, String material, int amount, String category) {
        // 这里的规格字符串是 Board 对象调用 getSpecification() 传入的:
        this.jdbcTemplate.update("INSERT INTO tb_inventory (specification, material, amount, category) VALUES (?, ?, ?, ?)", specification, material, amount, category);
    }

    public void updateInventoryAmount(Inventory inventory) {
        this.jdbcTemplate.update("UPDATE tb_inventory SET amount = ? WHERE id = ?", inventory.getAmount(), inventory.getId());
    }

    public void truncateInventory() {
        this.jdbcTemplate.update("TRUNCATE TABLE tb_inventory");
    }
}
